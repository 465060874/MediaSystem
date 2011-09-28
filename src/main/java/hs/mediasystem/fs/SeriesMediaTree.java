package hs.mediasystem.fs;

import hs.mediasystem.Constants;
import hs.mediasystem.GlowBorder;
import hs.mediasystem.db.CachedItemEnricher;
import hs.mediasystem.db.LocalItem;
import hs.mediasystem.db.TvdbSerieEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Renderer;
import hs.ui.controls.Label;
import hs.ui.controls.Picture;
import hs.ui.image.ImageHandle;

import java.awt.Color;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class SeriesMediaTree extends AbstractMediaTree {
  private final Path root;

  private List<? extends MediaItem> children;
  
  public SeriesMediaTree(Path root) {
    super(new CachedItemEnricher(new TvdbSerieEnricher()));
    this.root = root;
  }
  
  @Override
  public void refresh() {
    children = null;
  }

  @Override
  public Style getStyle() {
    return Style.BANNER;
  }

  @Override
  public List<? extends MediaItem> children() {
    if(children == null) {
      children = new SerieScanner(this).scan(root);
      Collections.sort(children, MediaItemComparator.INSTANCE);
    }
    
    return children;
  }

  @Override
  public MediaTree parent() {
    return null;
  }

  @Override
  public Renderer<MediaItem> getRenderer() {
    return new Renderer<MediaItem>() {
      private final Picture pic = new Picture();
      private final Label label = new Label();
      
      @Override
      public JComponent render(MediaItem item, boolean hasFocus) {
        Serie serie = (Serie)item;
        
        ImageHandle banner = serie.getBanner();
        
        if(banner != null) {
          pic.imageHandle.set(banner);
          pic.bgColor().set(new Color(0, 0, 0, 0));
          pic.minHeight().set(78);
          pic.maxHeight().set(78);

          if(hasFocus) {
            pic.border().set(new GlowBorder(Constants.MAIN_TEXT_COLOR.get(), 4));
          }
          else {
            pic.border().set(new EmptyBorder(4, 4, 4, 4));
          }
          
          return pic.getComponent();
        }
        else {
          label.text().set(serie.getTitle());
          label.font().set(Constants.LIST_LARGE_FONT);
          label.fgColor().link(Constants.MAIN_TEXT_COLOR);
          
          if(hasFocus) {
            label.border().set(new GlowBorder(Constants.MAIN_TEXT_COLOR.get(), 4));
          }
          else {
            label.border().set(new EmptyBorder(4, 4, 4, 4));
          }
          
          JLabel label2 = label.getComponent();
          
          label2.setPreferredSize(new Dimension(1, 78));
          
          return label2;
        }
      }

      @Override
      public MediaItem getPrototypeCellValue() {
        return new Serie(null, new LocalItem(null) {{
          setTitle(" ");
          setLocalTitle(" ");
        }});
      }
    };
  }
}
