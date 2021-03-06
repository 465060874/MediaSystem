package hs.mediasystem.framework;

import hs.mediasystem.util.io.RuntimeIOException;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class EpisodeScanner {
  private static final Pattern EXTENSION_PATTERN = Pattern.compile("(?i).+\\.(avi|flv|mkv|mov|mp4|mpg|mpeg)");
  private static final Set<FileVisitOption> FOLLOW_LINKS = new HashSet<FileVisitOption>() {{
    add(FileVisitOption.FOLLOW_LINKS);
  }};

  private final int maxDepth;

  public EpisodeScanner(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  public List<Path> scan(Path scanPath) {
    try {
      final List<Path> results = new ArrayList<>();

      Files.walkFileTree(scanPath, FOLLOW_LINKS, maxDepth, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if(!attrs.isDirectory() && file.getFileName().toString().matches(EXTENSION_PATTERN.pattern())) {
            results.add(file);
          }

          return FileVisitResult.CONTINUE;
        }
      });

      return results;
    }
    catch(IOException e) {
      throw new RuntimeIOException("Exception while scanning \"" + scanPath + "\"", e);
    }
  }
}
