package hs.mediasystem.framework;

import hs.mediasystem.db.Item;

import java.nio.file.Path;

public interface Decoder {
  Item decode(Path path);
}