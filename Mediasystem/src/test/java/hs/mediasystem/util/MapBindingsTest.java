package hs.mediasystem.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import org.junit.Before;
import org.junit.Test;

public class MapBindingsTest {

  public static class File {
    private final ObjectProperty<Video> video = new SimpleObjectProperty<>();
    public ObjectProperty<Video> videoProperty() { return video; }

    public File(Video video) {
      this.video.set(video);
    }
  }

  public static class Video {
    private final StringProperty title = new SimpleStringProperty();
    public StringProperty titleProperty() { return title; }

    private final ObjectProperty<Episode> episode = new SimpleObjectProperty<>();
    public ObjectProperty<Episode> episodeProperty() { return episode; }

    public final ObservableList<String> list = FXCollections.observableArrayList("A", "B", "C");

    public final ObservableMap<Class<?>, Object> map = FXCollections.observableHashMap();
    private final ObjectProperty<ObservableMap<Class<?>, Object>> dataMap = new SimpleObjectProperty<>(map);
    public ObjectProperty<ObservableMap<Class<?>, Object>> dataMapProperty() { return dataMap; }

    public Video(String title, Episode episode) {
      this.title.set(title);
      this.episode.set(episode);
    }
  }

  public static class Episode {
    private final IntegerProperty season = new SimpleIntegerProperty();
    public IntegerProperty seasonProperty() { return season; }

    public final IntegerProperty episodeNumber = new SimpleIntegerProperty(21);

    public Episode(int season) {
      this.season.set(season);
    }
  }

  public static class YouTube {
    private final IntegerProperty rating = new SimpleIntegerProperty();
    public IntegerProperty ratingProperty() { return rating; }

    public YouTube(int rating) {
      this.rating.set(rating);
    }
  }

  private File file1;
  private Episode episode1;

  @Before
  public void before() {
    episode1 = new Episode(5);
    file1 = new File(new Video("Alice", episode1));
    file1.videoProperty().get().dataMapProperty().get().put(YouTube.class, new YouTube(9));
  }

  @Test
  public void shouldGetTitle() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "title");

    assertEquals("Alice", select.get());
  }

  @Test
  public void shouldGetSeason() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "episode", "season");

    assertEquals(5, select.get());
  }

  @Test
  public void shouldGetEpisodeNumber() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "episode", "episodeNumber");

    assertEquals(21, select.get());
  }

  @Test
  public void shouldGetEpisodeNumberWithCombinedStep() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "episode.episodeNumber");

    assertEquals(21, select.get());
  }

  @Test(expected = RuntimeBindException.class)
  public void shouldThrowExceptionWhenMapExpectedButNotEncountered() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "episode[]", "episodeNumber");  // episode is not a map

    select.get();
  }

  /*
   * Check bad step formats
   */

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWithInvalidStepFormat1() {
    MapBindings.select(file1.videoProperty(), "[]");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWithInvalidStepFormat2() {
    MapBindings.select(file1.videoProperty(), "3Property");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWithInvalidStepFormat3() {
    MapBindings.select(file1.videoProperty(), ".a.b.c");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWithInvalidStepFormat4() {
    MapBindings.select(file1.videoProperty(), "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWithInvalidStepFormat5() {
    MapBindings.select(file1.videoProperty(), 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWithInvalidStepFormat6() {
    MapBindings.select(file1.videoProperty(), (Object)null);
  }

  /*
   * List Tests
   */

  @Test
  public void shouldGetListValue() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "list[]", 1);

    assertEquals("B", select.get());
  }

  @Test
  public void shouldGetNullWhenListIndexOutOfRange() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "list[]", 15);

    assertNull(select.get());
  }

  @Test(expected = RuntimeBindException.class)
  public void shouldThrowExceptionWhenListIndexIsNegative() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "list[]", -1);

    assertNull(select.get());
  }

  @Test(expected = RuntimeBindException.class)
  public void shouldThrowExceptionWhenListIndexIsNotInteger() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "list[]", "a");

    select.get();
  }

  /*
   * Map Tests
   */

  @Test
  public void shouldGetMapValue() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "dataMap[]", YouTube.class, "rating");

    assertEquals(9, select.get());
  }

  @Test
  public void shouldUpdateBindingWhenMapChanges() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "dataMap[]", YouTube.class, "rating");

    assertEquals(9, select.get());

    ObservableMap<Class<?>, Object> observableHashMap = FXCollections.observableHashMap();
    observableHashMap.put(YouTube.class, new YouTube(7));

    file1.videoProperty().get().dataMapProperty().set(observableHashMap);

    assertEquals(7, select.get());
  }

  @Test
  public void shouldGetMapValueDirectly() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "map[]", YouTube.class, "rating");

    assertEquals(9, select.get());
  }

  @Test
  public void shouldSelectFromMap() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty().get().map, YouTube.class, "rating");

    assertEquals(9, select.get());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWhenMapKeyIsMissing() {
    MapBindings.select(file1.videoProperty(), "map[]");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWhenMapKeyIsMissingWhenRootIsMap() {
    MapBindings.select(file1.videoProperty().get().map);
  }

  @Test
  public void shouldUpdateBindingWhenVideoChanges() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "dataMap[]", YouTube.class, "rating");

    assertEquals(9, select.get());

    Video v = new Video("Something", null);
    v.dataMapProperty().get().put(YouTube.class, new YouTube(0));
    file1.videoProperty().set(v);

    assertEquals(0, select.get());
  }

  @Test
  public void shouldUpdateBindingWhenMapKeyChanges() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "map[]", YouTube.class, "rating");

    assertEquals(9, select.get());

    file1.videoProperty().get().dataMapProperty().get().put(YouTube.class, new YouTube(8));

    assertEquals(8, select.get());

    file1.videoProperty().get().dataMapProperty().get().remove(YouTube.class);

    assertNull(select.get());

    file1.videoProperty().get().dataMapProperty().get().put(YouTube.class, new YouTube(7));

    assertEquals(7, select.get());
  }

  @Test
  public void shouldReturnNullWhenAnyStepNull() {
    ObjectBinding<Object> seasonBinding = MapBindings.select(file1.videoProperty(), "episode", "season");
    ObjectBinding<Object> titleBinding = MapBindings.select(file1.videoProperty(), "title");
    ObjectBinding<Object> episodeBinding = MapBindings.select(file1.videoProperty(), "episode");

    assertEquals(5, seasonBinding.get());
    assertEquals("Alice", titleBinding.get());
    assertEquals(episode1, episodeBinding.get());

    file1.videoProperty().get().episodeProperty().set(null);

    assertNull(seasonBinding.get());
    assertEquals("Alice", titleBinding.get());
    assertNull(episodeBinding.get());

    file1.videoProperty().set(null);

    assertNull(seasonBinding.get());
    assertNull(titleBinding.get());
    assertNull(episodeBinding.get());
  }

  @Test
  public void shouldUpdateBindingWhenStepIsAltered() {
    ObjectBinding<Object> seasonBinding = MapBindings.select(file1.videoProperty(), "episode", "season");
    ObjectBinding<Object> titleBinding = MapBindings.select(file1.videoProperty(), "title");
    ObjectBinding<Object> episodeBinding = MapBindings.select(file1.videoProperty(), "episode");

    file1.videoProperty().set(null);

    assertNull(seasonBinding.get());
    assertNull(titleBinding.get());
    assertNull(episodeBinding.get());

    file1.videoProperty().set(new Video("Bob", null));

    assertNull(seasonBinding.get());
    assertEquals("Bob", titleBinding.get());
    assertNull(episodeBinding.get());

    file1.videoProperty().get().titleProperty().set("Cassie");

    assertNull(seasonBinding.get());
    assertEquals("Cassie", titleBinding.get());
    assertNull(episodeBinding.get());

    Episode episode15 = new Episode(15);
    file1.videoProperty().get().episodeProperty().set(episode15);

    assertEquals(15, seasonBinding.get());
    assertEquals("Cassie", titleBinding.get());
    assertEquals(episode15, episodeBinding.get());
  }

  public static class MockObservableValue implements ObservableValue<String> {
    private final List<InvalidationListener> listeners = new ArrayList<>();

    private String value;
    private boolean invalidated = true;

    @Override
    public void addListener(InvalidationListener listener) {
      listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
      listeners.remove(listener);
    }

    @Override
    public void addListener(ChangeListener<? super String> listener) {
    }

    @Override
    public void removeListener(ChangeListener<? super String> listener) {
    }

    public int getListenerCount() {
      return listeners.size();
    }

    @Override
    public String getValue() {
      this.invalidated = false;
      return value;
    }

    public void setValue(String value) {
      this.value = value;

      if(!invalidated) {
        for(InvalidationListener listener : new ArrayList<>(listeners)) {
          listener.invalidated(this);
        }
      }

      this.invalidated = true;
    }
  }

  @Test
  public void shouldOnlyRegisterListenerOnce() {
    MockObservableValue mock = new MockObservableValue();

    StringBinding selectString = MapBindings.selectString(mock);

    assertEquals(0, mock.getListenerCount());

    assertNull(selectString.getValue());

    assertEquals(1, mock.getListenerCount());

    mock.setValue("bla");

    assertEquals(0, mock.getListenerCount());

    mock.setValue("bla2");
    mock.setValue("bla3");

    assertEquals(0, mock.getListenerCount());

    assertEquals("bla3", selectString.getValue());

    assertEquals(1, mock.getListenerCount());
  }

  /*
   * Builder tests
   */

  @Test
  public void shouldGetTitleWithBuilder() {
    StringBinding stringBinding = MapBindings.get(file1.videoProperty()).then("title").asStringBinding();

    assertEquals("Alice", stringBinding.get());
  }

  @Test
  public void shouldGetDefaultWithBuilder() {
    StringBinding stringBinding = MapBindings.get(file1.videoProperty()).thenOrDefault("nonExistingField", "Alice").asStringBinding();

    assertEquals("Alice", stringBinding.get());
  }

  @Test
  public void shouldGetNullDefaultWithBuilder() {
    StringBinding stringBinding = MapBindings.get(file1.videoProperty()).thenOrDefault("nonExistingField", null).asStringBinding();

    assertNull(stringBinding.get());
  }

  @Test
  public void shouldGetListValueWithBuilder() {
    ObjectBinding<Object> select = MapBindings.get(file1.videoProperty()).then("list").lookup(1).asObjectBinding();

    assertEquals("B", select.get());
  }

  @Test
  public void shouldGetMapValueWithBuilder() {
    ObjectBinding<Object> select = MapBindings.get(file1.videoProperty()).then("dataMap").lookup(YouTube.class).then("rating").asObjectBinding();

    assertEquals(9, select.get());
  }

}
