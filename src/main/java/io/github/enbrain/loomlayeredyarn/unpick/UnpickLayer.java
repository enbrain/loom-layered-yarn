package io.github.enbrain.loomlayeredyarn.unpick;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.mappingio.MappingVisitor;

public record UnpickLayer(Path sourcePath, String constantsDependency) implements MappingLayer {
    public static final String UNPICK_DEFINITION_PATH = "extras/definitions.unpick";
    public static final String UNPICK_METADATA_PATH = "extras/unpick.json";

    @Override
    public void visit(MappingVisitor mappingVisitor) throws IOException {
    }

    public byte[] getUnpickDefinition() {
        return this.read(UNPICK_DEFINITION_PATH);
    }

    public byte[] getUnpickMetadata() {
        return this.read(UNPICK_METADATA_PATH);
    }

    public String getConstantsDependency() {
        return this.constantsDependency;
    }

    private byte[] read(String path) {
        try (ZipFile zipFile = new ZipFile(sourcePath.toFile())) {
            ZipEntry zipFileEntry = zipFile.getEntry(path);
            Objects.requireNonNull(zipFileEntry, "Could not find %s in file".formatted(path));

            try (InputStream stream = zipFile.getInputStream(zipFileEntry)) {
                return stream.readAllBytes();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract unpick", e);
        }
    }
}
