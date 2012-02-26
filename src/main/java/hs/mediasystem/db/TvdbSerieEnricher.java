package hs.mediasystem.db;

import java.util.List;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Series;

public class TvdbSerieEnricher implements ItemEnricher<Object> {

  @Override
  public String getProviderCode() {
    return "TVDB";
  }

  @Override
  public String identifyItem(final LocalInfo<Object> localInfo) throws IdentifyException {
    synchronized(TheTVDB.class) {
      final String name = localInfo.getTitle();

      TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

      List<Series> results = tvDB.searchSeries(name, "en");

      System.out.println("TVDB results: " + results);

      if(results.isEmpty()) {
        throw new IdentifyException(localInfo);
      }

      return results.get(0).getId();
    }
  }

  @Override
  public Item loadItem(String identifier, LocalInfo<Object> localInfo) throws ItemNotFoundException {
    synchronized(TheTVDB.class) {
      TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

      final Series series = tvDB.getSeries(identifier, "en");

      if(series == null) {
        throw new ItemNotFoundException(identifier);
      }

      byte[] banner = Downloader.tryReadURL(series.getBanner());
      byte[] poster = Downloader.tryReadURL(series.getPoster());
      byte[] background = Downloader.tryReadURL(series.getFanart());

      Item item = new Item();

      item.setTitle(series.getSeriesName());
      item.setRating(Float.valueOf(series.getRating()));
      item.setPlot(series.getOverview());

      item.setBanner(new MemorySource<>(banner));
      item.setPoster(new MemorySource<>(poster));
      item.setBackground(new MemorySource<>(background));

      item.setGenres(series.getGenres().toArray(new String[series.getGenres().size()]));
      item.setLanguage(series.getLanguage());
      item.setRuntime(Integer.parseInt(series.getRuntime()));
      System.out.println(">>> Status of serie : " + series.getStatus());  // "Ended", "Continuing"
      System.out.println(">>> First aired : " +  series.getFirstAired());

      return item;
    }
  }
}
