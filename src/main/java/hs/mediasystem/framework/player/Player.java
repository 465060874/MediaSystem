package hs.mediasystem.framework.player;

import java.nio.file.Path;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.collections.ObservableList;

public interface Player {

  void play(String uri);

  void pause();

  boolean isPlaying();

  void stop();

  void dispose();

  void showSubtitle(Path path);

  /**
   * Returns the length of the stream in milliseconds.
   *
   * @return the length of the stream in milliseconds
   */
  long getLength();
  ReadOnlyLongProperty lengthProperty();

  /**
   * Returns the position of the stream in milliseconds.
   *
   * @return the position of the stream in milliseconds
   */
  long getPosition();
  void setPosition(long position);
  LongProperty positionProperty();

  /**
   * Returns the current volume in percent.
   *
   * @return the current volume in percent
   */
  int getVolume();
  void setVolume(int volume);
  IntegerProperty volumeProperty();

  boolean isMute();
  void setMute(boolean mute);

  /**
   * Returns the current subtitle delay in milliseconds.
   *
   * @return the current subtitle delay in milliseconds
   */
  int getSubtitleDelay();
  void setSubtitleDelay(int delay);
  IntegerProperty subtitleDelayProperty();

  /**
   * Returns a list of Subtitles.  This list always includes as the first element
   * Subtitle.DISABLED.  This list is therefore never empty.
   *
   * @return a list of Subtitles
   */
  ObservableList<Subtitle> getSubtitles();

  /**
   * Returns the current subtitle.  Will return a Subtitle.DISABLED when not showing any
   * subtitle.
   *
   * @return the current subtitle
   */
  Subtitle getSubtitle();
  void setSubtitle(Subtitle subtitle);
  ObjectProperty<Subtitle> subtitleProperty();

  /**
   * Returns the current rate of playback.
   *
   * @return the current rate of playback
   */
  float getRate();
  void setRate(float rate);
  FloatProperty rateProperty();

  /**
   * Returns the audio delay, in milliseconds.
   *
   * @return the audio delay, in milliseconds
   */
  int getAudioDelay();
  void setAudioDelay(int rate);
  IntegerProperty audioDelayProperty();

  AudioTrack getAudioTrack();
  void setAudioTrack(AudioTrack audioTrack);
  ObjectProperty<AudioTrack> audioTrackProperty();

  /**
   * Returns the brightness.
   *
   * @return the brightness
   */
  float getBrightness();
  void setBrightness(float brightness);
  FloatProperty brightnessProperty();

  ObservableList<AudioTrack> getAudioTracks();

  void setScreen(int screenNumber);
}
