package io.github.enbrain.loomlayeredyarn.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;

public class LocalDirectorySpec implements FileSpec {
    private final Path directory;
    private final int hash;

    public LocalDirectorySpec(Path directory) {
        this.directory = directory;
        this.hash = calculateHashCode();
    }

    private int calculateHashCode() {
        if (!Files.exists(directory)) {
            throw new RuntimeException(
                    "Could not find %s, it must be present at spec creation time to calculate mappings hash"
                            .formatted(directory.toAbsolutePath()));
        }

        var lastModifiedVisitor = new SimpleFileVisitor<Path>() {
            private FileTime latestFileTime = null;

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                FileTime lastModified = Files.getLastModifiedTime(file);

                if (this.latestFileTime == null || lastModified.compareTo(this.latestFileTime) > 0) {
                    this.latestFileTime = lastModified;
                }

                return FileVisitResult.CONTINUE;
            }

            public FileTime getLatestFileTime() {
                return this.latestFileTime;
            }
        };

        try {
            Files.walkFileTree(directory, lastModifiedVisitor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Objects.hash(lastModifiedVisitor.getLatestFileTime(), directory.toAbsolutePath());
    }

    @Override
    public Path get(MappingContext context) {
        return directory;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
