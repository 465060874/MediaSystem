package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.enrich.InstanceEnricher;
import hs.mediasystem.framework.EnrichCallback;
import hs.mediasystem.framework.EnricherBuilder;
import hs.mediasystem.framework.EntityFactory;
import hs.mediasystem.framework.FinishEnrichCallback;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.fs.EpisodeScanner;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SerieItem extends MediaItem implements MediaRoot {
  private final SeriesMediaTree mediaRoot;
  private final EntityFactory entityFactory;
  private final ItemsDao itemsDao;

  private List<MediaItem> children;

  public SerieItem(SeriesMediaTree mediaTree, String uri, Serie serie, EntityFactory entityFactory, ItemsDao itemsDao) {
    super(mediaTree, uri, serie);

    this.mediaRoot = mediaTree;
    this.entityFactory = entityFactory;
    this.itemsDao = itemsDao;
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new EpisodeScanner(new EpisodeDecoder(getTitle()), 2).scan(Paths.get(getUri()));

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        final Episode episode = new Episode(this, getTitle() + " " + localInfo.getSeason() + "x" + localInfo.getEpisode(), localInfo.getSeason(), localInfo.getEpisode(), localInfo.getEndEpisode());

        episode.setEntityFactory(entityFactory);

        final MediaItem mediaItem = new MediaItem(getMediaTree(), localInfo.getUri(), episode);

        InstanceEnricher<Episode, Void> enricher = new EnricherBuilder<Episode, Item>()
            .require(mediaItem.dataMapProperty().get(), Identifier.class)
            .enrich(new EnrichCallback<Item>() {
              @Override
              public Item enrich(Object... parameters) {
                Identifier identifier = (Identifier)parameters[0];

                if(identifier.getProviderId() != null) {
                  try {
                    // FIXME should check version after loading, and bypasscache
                    return itemsDao.loadItem(identifier.getProviderId());
                  }
                  catch(ItemNotFoundException e) {
                    return null;
                  }
                }

                return null;
              }
            })
            .enrich(new EnrichCallback<Item>() {
              @Override
              public Item enrich(Object... parameters) {
                Identifier identifier = (Identifier)parameters[0];

                if(identifier.getProviderId() != null) {
                  try {
                    Item item = Activator.TVDB_EPISODE_ENRICHER.loadItem(identifier.getProviderId());

                    if(item != null) {
                      itemsDao.storeItem(item);  // FIXME should also update if version or bypasscache problem
                    }

                    return item;
                  }
                  catch(ItemNotFoundException e) {
                    System.out.println("[FINE] Item not found (in Database or TVDB): " + identifier.getProviderId());
                  }
                }

                return null;
              }
            })
            .finish(new FinishEnrichCallback<Item>() {
              @Override
              public void update(Item result) {
                episode.item.set(result);
              }
            })
            .build();

        episode.setEnricher(enricher);

        children.add(mediaItem);
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return getTitle();
  }

  @Override
  public String getId() {
    return "serie[" + getTitle() + "]";
  }

  @Override
  public MediaRoot getParent() {
    return mediaRoot;
  }

}
