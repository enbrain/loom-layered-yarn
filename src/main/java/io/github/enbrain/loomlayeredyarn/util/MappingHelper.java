package io.github.enbrain.loomlayeredyarn.util;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import net.fabricmc.loom.util.ZipUtils;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;

public final class MappingHelper {
    private MappingHelper() {
    }

    public static void read(Path path, MappingVisitor visitor) throws IOException {
        findMappingPath(path, p -> {
            try {
                MappingReader.read(p, visitor);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void findMappingPath(Path path, Consumer<Path> consumer) throws IOException {
        if (ZipUtils.isZip(path)) {
            try (FileSystem fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
                Path p = fs.getPath("mappings/mappings.tiny");
                if (!Files.exists(p)) {
                    p = fs.getPath("/");
                }

                consumer.accept(p);
            }
        } else {
            consumer.accept(path);
        }
    }
}
