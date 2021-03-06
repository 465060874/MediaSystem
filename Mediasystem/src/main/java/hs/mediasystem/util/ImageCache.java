package hs.mediasystem.util;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageCache {
  private static final ReferenceQueue<CompletableFuture<Image>> REFERENCE_QUEUE = new ReferenceQueue<>();
  private static final SortedMap<String, WeakReference<CompletableFuture<Image>>> CACHE = new TreeMap<>();

  public static Image loadImage(ImageHandle handle) {
    cleanReferenceQueue();

    return loadImage(handle.getKey(), () -> new Image(new ByteArrayInputStream(handle.getImageData())));
  }

  public static Image loadImageUptoMaxSize(ImageHandle handle, int w, int h) {
    cleanReferenceQueue();

    String key = createKey(handle.getKey(), w, h, true);

    return loadImage(key, () -> {
      Image image = null;
      byte[] data = handle.getImageData();

      if(data != null) {
        Dimension size = determineSize(data);

        if(size != null) {
          if(size.width <= w && size.height <= h) {
            image = new Image(new ByteArrayInputStream(data));
          }
          else {
            image = new Image(new ByteArrayInputStream(data), w, h, true, true);
          }
        }
      }

      return image;
    });
  }

  /**
   * Loads the image matching the given key, optionally using the Image supplier if no previous
   * load was in progress.  If another load for the same image is in progress this function
   * waits for it to be completed and then returns the image.
   *
   * @param key an image key
   * @param imageSupplier an Image supplier, which will be called if needed
   * @return the image matching the given key
   */
  private static Image loadImage(String key, Supplier<Image> imageSupplier) {
    CompletableFuture<Image> futureImage;
    boolean needsCompletion = false;

    /*
     * Obtain an existing Future for the Image to be loaded or create a new one:
     */

    synchronized(CACHE) {
      WeakReference<CompletableFuture<Image>> futureImageRef = CACHE.get(key);

      futureImage = futureImageRef != null ? futureImageRef.get() : null;

      if(futureImage == null) {
        futureImage = new CompletableFuture<>();
        store(key, futureImage);
        needsCompletion = true;
      }
    }

    /*
     * Optionally trigger an image load and use it to complete the Future.  This must happen
     * outside the synchronized block as the lock would otherwise be held for the duration
     * of the image loading.
     */

    if(needsCompletion) {
      Image image = null;

      try {
        image = imageSupplier.get();

        futureImage.complete(image);
      }
      catch(Exception e) {
        futureImage.completeExceptionally(e);
      }

      if(image == null) {
        synchronized(CACHE) {
          CACHE.remove(key);
        }
      }
    }

    /*
     * Get the final image result and return it:
     */

    try {
      return futureImage.get();
    }
    catch(InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private static Dimension determineSize(byte[] data) {
    try(ImageInputStream is = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
      Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(is);

      if(imageReaders.hasNext()) {
        ImageReader imageReader = imageReaders.next();

        imageReader.setInput(is);

        int w = imageReader.getWidth(imageReader.getMinIndex());
        int h = imageReader.getHeight(imageReader.getMinIndex());

        imageReader.dispose();

        return new Dimension(w, h);
      }

      return null;
    }
    catch(IOException e) {
      return null;
    }
  }

  private static void store(String key, CompletableFuture<Image> imageFuture) {
    ImageFutureWeakReference imageRef = new ImageFutureWeakReference(key, imageFuture, REFERENCE_QUEUE);

    synchronized(CACHE) {
      CACHE.put(key, imageRef);
    }
  }

  private static void cleanReferenceQueue() {
    int size;
    int counter = 0;

    synchronized(CACHE) {
      size = CACHE.size();

      for(;;) {
        ImageFutureWeakReference ref = (ImageFutureWeakReference)REFERENCE_QUEUE.poll();

        if(ref == null) {
          break;
        }

        CACHE.remove(ref.getKey());

        counter++;
      }
    }

    if(counter > 0) {
      System.out.println("[FINE] ImageCache.cleanReferenceQueue() - Removed " + counter + "/" + size + " images.");
    }
  }

  private static String createKey(String baseKey, double w, double h, boolean keepAspect) {
    return baseKey + "#" + w + "x" + h + "-" + (keepAspect ? "T" : "F");
  }

  public static void expunge(ImageHandle handle) {
    if(handle != null) {
      String keyToRemove = handle.getKey();

      synchronized(CACHE) {
        for(Iterator<Map.Entry<String, WeakReference<CompletableFuture<Image>>>> iterator = CACHE.tailMap(keyToRemove).entrySet().iterator(); iterator.hasNext();) {
          Map.Entry<String, WeakReference<CompletableFuture<Image>>> entry = iterator.next();

          if(!entry.getKey().startsWith(keyToRemove)) {
            break;
          }

          iterator.remove();
        }
      }
    }
  }

  private static class ImageFutureWeakReference extends WeakReference<CompletableFuture<Image>> {
    private final String key;

    public ImageFutureWeakReference(String key, CompletableFuture<Image> referent, ReferenceQueue<? super CompletableFuture<Image>> q) {
      super(referent, q);

      this.key = key;
    }

    public String getKey() {
      return key;
    }
  }
}
