package io.github.enbrain.loomlayeredyarn.unpick;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.mappingio.MappingVisitor;

public class UnpickLayer implements MappingLayer {
    public static final String UNPICK_DEFINITION_PATH = "extras/definitions.unpick";
    public static final String UNPICK_METADATA_PATH = "extras/unpick.json";

    private final Path sourcePath;
    private final String constantsDependency;
    private boolean enabled = false;

    public UnpickLayer(Path sourcePath, String constantsDependency) {
        this.sourcePath = sourcePath;
        this.constantsDependency = constantsDependency;
    }

    @Override
    public void visit(MappingVisitor mappingVisitor) throws IOException {
        if (!this.enabled) {
            throw new IllegalStateException("Unpick layer is not enabled");
        }
    }

    public void enable() {
        this.enabled = true;
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
