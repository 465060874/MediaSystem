package hs.mediasystem.screens;

import hs.mediasystem.util.LifoBlockingDeque;
import hs.mediasystem.util.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class GroupWorker extends Service<Void> {
  private final ThreadPoolExecutor executor;
  private final List<Task<?>> activeTasks = new ArrayList<>();  // Finished, executing and/or waiting tasks
  private final Map<Object, NodeList<KeyedTask>.Node> taskNodeMap = new HashMap<>();  // Quick key based Node lookup
  private final NodeList<KeyedTask> taskQueue = new NodeList<>();  // Tasks waiting, head is most recently added/promoted
  private final String title;
  private final int maxThreads;

  private long startTaskCount;

  public GroupWorker(String title, int maxThreads) {
    this.title = title;
    this.maxThreads = maxThreads;
    executor = new ThreadPoolExecutor(maxThreads, maxThreads, 5, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>(), new ThreadFactory() {
      private int threadNumber;

      @Override
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "GroupWorker-thread-" + ++threadNumber);
        thread.setDaemon(true);
        return thread;
      }
    });

    new ActivityMonitorThread().start();
  }

  public void submit(Object key, final Task<?> task) {
    synchronized(activeTasks) {
      activeTasks.add(task);
      taskNodeMap.put(key, taskQueue.addHead(new KeyedTask(key, task)));
    }

    task.stateProperty().addListener(new ChangeListener<State>() {
      @Override
      public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
        if(newValue == State.FAILED) {
          System.out.println("[WARN] Worker " + task.getTitle() + " failed with exception: " + task.getException());
          task.getException().printStackTrace(System.out);
        }
      }
    });
  }

  public void promote(Object key) {
    synchronized(activeTasks) {
      KeyedTask keyedTask = taskQueue.unlink(taskNodeMap.remove(key));

      if(keyedTask != null) {
        taskNodeMap.put(key, taskQueue.addHead(keyedTask));
      }
    }
  }

  public void cancel(Object key) {
    synchronized(activeTasks) {
      taskQueue.unlink(taskNodeMap.remove(key));
    }
  }

  @Override
  protected Task<Void> createTask() {
    return new Task<Void>() {
      {
        updateTitle(title + " (0/1)");
        updateProgress(0, 1);
      }

      @Override
      protected Void call() throws InterruptedException {
        for(;;) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              synchronized(activeTasks) {
                long completed = executor.getCompletedTaskCount() - startTaskCount;
                long total = activeTasks.size();
                double totalProgress = 0.0;
                double totalWork = 0.0;
                String message = "";

                for(Task<?> task : activeTasks) {
                  String title = task.getTitle();
                  double p = task.getProgress();

                  if(!task.isDone() && !title.isEmpty()) {
                    message += "• " + title + "\n";
                  }

                  if(p >= 0) {
                    totalProgress += p;
                  }
                  totalWork++;
                }

                updateTitle(title + " (" + completed + "/" + total +")");
                updateMessage(message);
                updateProgress((long)(totalProgress * 256), (long)(totalWork * 256));
              }
            }
          });

          synchronized(activeTasks) {
            int slotsAvailable = maxThreads - executor.getActiveCount() - executor.getQueue().size();

            while(slotsAvailable-- > 0 && !taskNodeMap.isEmpty()) {
              KeyedTask keyedTask = taskQueue.removeHead();
              taskNodeMap.remove(keyedTask.getKey());
              executor.execute(keyedTask.getTask());
            }

            if(executor.getCompletedTaskCount() == executor.getTaskCount() && taskNodeMap.isEmpty()) {
              activeTasks.clear();
              startTaskCount = executor.getTaskCount();
              return null;
            }
          }

          Thread.sleep(100);
        }
      }
    };
  }

  private final class ActivityMonitorThread extends Thread {
    public ActivityMonitorThread() {
      setName("GroupWorker-activity-monitor");
      setDaemon(true);
    }

    @Override
    public void run() {
      try {
        for(;;) {
          if(!taskNodeMap.isEmpty()) {
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                synchronized(activeTasks) {
                  if(!taskNodeMap.isEmpty() && !isRunning()) {
                    restart();
                  }
                }
              }
            });
          }

          Thread.sleep(100);
        }
      }
      catch(InterruptedException e) {
        System.out.println("[FINE] " + this + " interrupted");
      }
    }
  }

  private static class KeyedTask {
    private final Object key;
    private final Task<?> task;

    public KeyedTask(Object key, Task<?> task) {
      this.key = key;
      this.task = task;
    }

    public Object getKey() {
      return key;
    }

    public Task<?> getTask() {
      return task;
    }
  }
}