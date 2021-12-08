package io.github.enbrain.loomlayeredyarn.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;

public final class MappingHelper {
    private MappingHelper() {
    }

    public static void read(Path path, MappingVisitor visitor) throws IOException {
        if (isZip(path)) {
            try (FileSystem fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
                Path p = fs.getPath("mappings/mappings.tiny");
                if (!Files.exists(p)) {
                    p = fs.getPath("/");
                }

                MappingReader.read(p, visitor);
            }
        } else {
            MappingReader.read(path, visitor);
        }
    }

    private static boolean isZip(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            return false;
        }

        try (Reader reader = new InputStreamReader(Files.newInputStream(path))) {
            int c1 = reader.read();
            int c2 = reader.read();
            int c3 = reader.read();
            int c4 = reader.read();

            if ((c1 | c2 | c3 | c4) < 0) {
                return false;
            }

            int signature = (c1 << 24) + (c2 << 16) + (c3 << 8) + (c4 << 0);
            return signature == 0x504B0304 || signature == 0x504B0506 || signature == 0x504B0708;
        }
    }
}
