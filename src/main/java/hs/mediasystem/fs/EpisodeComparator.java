package hs.mediasystem.fs;

import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;

public class EpisodeComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new EpisodeComparator();

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    int result = Integer.compare(o1.getSeason() != null ? o1.getSeason() : Integer.MAX_VALUE, o2.getSeason() != null ? o2.getSeason() : Integer.MAX_VALUE);

    if(result == 0) {
      result = Integer.compare(o1.getEpisode(), o2.getEpisode());

      if(result == 0) {
        result = o1.getTitle().compareTo(o2.getTitle());
      }
    }

    return result;
  }

}