package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.Media;
import hs.mediasystem.screens.selectmedia.StandardDetailPaneDecorator;
import hs.mediasystem.util.AreaPane;
import hs.mediasystem.util.MapBindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class EpisodeDetailPaneDecorator extends StandardDetailPaneDecorator {
  protected final StringBinding season = MapBindings.selectInteger(dataProperty(), "season").asString();
  protected final StringBinding episode = MapBindings.selectString(dataProperty(), "episodeRange");

  public EpisodeDetailPaneDecorator() {
    groupName.bind(MapBindings.selectString(dataProperty(), "serie", "dataMap", Media.class, "title"));
  }

  @Override
  public void decorate(AreaPane areaPane) {
    super.decorate(areaPane);

    areaPane.getStylesheets().add("select-media/episode-detail-pane.css");  // TODO move to bundle when possible

    areaPane.add("title-area", 10, createSeasonEpisodeBlock());
  }

  protected Pane createSeasonEpisodeBlock() {
    final Label seasonLabel = new Label() {{
      getStyleClass().addAll("field", "season");
      setMaxWidth(10000);
      textProperty().bind(season);
    }};

    final Label episodeLabel = new Label() {{
      getStyleClass().addAll("field", "episode");
      setMaxWidth(10000);
      textProperty().bind(episode);
    }};

    return new FlowPane() {{
      getStyleClass().add("fields");
      getChildren().add(createTitledBlock("SEASON", seasonLabel, null));
      getChildren().add(createTitledBlock("EPISODE", episodeLabel, null));
    }};
  }
}