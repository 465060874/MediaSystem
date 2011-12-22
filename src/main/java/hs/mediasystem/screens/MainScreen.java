package hs.mediasystem.screens;

import hs.mediasystem.ProgramController;
import hs.mediasystem.fs.MoviesMediaTree;
import hs.mediasystem.fs.SeriesMediaTree;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainScreen {
  private final ProgramController controller;

  public MainScreen(ProgramController controller) {
    this.controller = controller;
  }
  
  public Node create() {
    final BorderPane root = new BorderPane();
    
    final List<Button> buttons = new ArrayList<Button>() {{
      add(new Button("Movies") {{
        setGraphic(new ImageView(new Image("images/package_multimedia.png")));
      }});
      add(new Button("Series") {{
        setGraphic(new ImageView(new Image("images/aktion.png")));
      }});
      add(new Button("Nederland 24") {{
        setGraphic(new ImageView(new Image("images/browser.png")));
      }});
      add(new Button("Youtube") {{
        setGraphic(new ImageView(new Image("images/tv.png")));
      }});
    }};
    

    final VBox menuBox = new VBox() {{
      getChildren().addAll(buttons);
    }};

    HBox box = new HBox() {{
      getChildren().add(menuBox);
      getChildren().add(new ProgressBar() {{
        setProgress(0.25);
        setMinSize(300, 30);
        setStyle("-fx-background-color: black;");
        getStyleClass().add("position");
        setTranslateY(10);
      }});
    }};

    ChangeListener<Boolean> changeListener = new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        Timeline timeline = new Timeline();
        Button b = (Button)(((ReadOnlyProperty<? extends Boolean>)observable).getBean());
        
        double d = b.getParent().getLayoutBounds().getHeight();
        timeline.getKeyFrames().addAll(
          new KeyFrame(Duration.ZERO, new KeyValue(menuBox.translateYProperty(), menuBox.getTranslateY())),
          new KeyFrame(new Duration(500), new KeyValue(menuBox.translateYProperty(), -b.getLayoutY() + d / 2))
        );
        
        timeline.play();
      }
    };
    
    for(final Button button : buttons) {
      button.focusedProperty().addListener(changeListener);
//      button.addEventHandler(EventType.ROOT, new EventHandler<Event>() {
//        @Override
//        public void handle(Event event) {
//          System.out.println("Button " + button.getText() + " received " + event);
//        }
//      });
    }
    
    buttons.get(0).setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        controller.showSelectItemScreen(new MoviesMediaTree(Paths.get(controller.getIni().getValue("general", "movies.path"))));
      }
    });
    
    buttons.get(1).setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        controller.showSelectItemScreen(new SeriesMediaTree(Paths.get(controller.getIni().getValue("general", "series.path"))));
      }
    });
    
    root.setCenter(box);
    
    root.setTop(new HBox() {{
      setId("top-bar");
      getChildren().add(new Label("MediaSystem"));
    }});
    
    return root;
  }
}