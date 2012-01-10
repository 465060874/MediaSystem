package hs.mediasystem.db;

import javax.inject.Inject;


public class CachedItemEnricher implements ItemEnricher {
  private final ItemsDao itemsDao;
  private final ItemEnricher providerToCache;

  @Inject
  public CachedItemEnricher(ItemsDao itemsDao, ItemEnricher providerToCache) {
    this.itemsDao = itemsDao;
    this.providerToCache = providerToCache;
  }

  @Override
  public void enrichItem(Item item) throws ItemNotFoundException {
    String fileName = item.getPath().getFileName().toString();

    try {
      System.out.println("[FINE] Resolving from database cache: " + fileName);
      Item cachedItem = itemsDao.getItem(item.getPath());

      item.setId(cachedItem.getId());
      item.setVersion(cachedItem.getVersion());
      item.setBackground(cachedItem.getBackground());
      item.setPoster(cachedItem.getPoster());
      item.setBanner(cachedItem.getBanner());
      item.setImdbId(cachedItem.getImdbId());
      item.setPlot(cachedItem.getPlot());
      item.setTitle(cachedItem.getTitle());
      item.setRating(cachedItem.getRating());
      item.setReleaseDate(cachedItem.getReleaseDate());
      item.setRuntime(cachedItem.getRuntime());
      item.setSeason(cachedItem.getSeason());
      item.setEpisode(cachedItem.getEpisode());
      item.setType(cachedItem.getType());
      item.setSubtitle(cachedItem.getSubtitle());
      item.setProvider(cachedItem.getProvider());
      item.setProviderId(cachedItem.getProviderId());
      item.setProviderParentId(cachedItem.getProviderParentId());

      if(cachedItem.getVersion() < ItemsDao.VERSION) {
        System.out.println("[FINE] Old version, updating from cached provider: " + fileName);

        providerToCache.enrichItem(item);
        itemsDao.updateItem(cachedItem);
      }
    }
    catch(ItemNotFoundException e) {
      System.out.println("[FINE] Cache miss, falling back to cached provider: " + fileName);

      providerToCache.enrichItem(item);
      itemsDao.storeItem(item);
    }
    catch(Exception e) {
      System.out.println("[WARN] Enrichment failed: " + e);
      throw new RuntimeException(e);
    }
  }
}
