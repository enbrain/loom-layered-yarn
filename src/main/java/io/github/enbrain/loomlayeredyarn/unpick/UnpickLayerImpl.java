package io.github.enbrain.loomlayeredyarn.unpick;

import java.io.IOException;
import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.configuration.providers.mappings.extras.unpick.UnpickLayer;
import net.fabricmc.loom.configuration.providers.mappings.file.FileMappingsLayer;
import net.fabricmc.loom.util.ZipUtils;
import net.fabricmc.mappingio.MappingVisitor;

public record UnpickLayerImpl(Path sourcePath, String constantsDependency) implements MappingLayer, UnpickLayer {
    @Override
    public void visit(MappingVisitor mappingVisitor) throws IOException {
        // Nothing to do here
    }

    @Override
    public @Nullable UnpickData getUnpickData() throws IOException {
        if (!ZipUtils.isZip(this.sourcePath)) {
            throw new UnsupportedOperationException("Unpick source must be a zip file");
        }

        FileMappingsLayer delegate = new FileMappingsLayer(sourcePath, null, null, null, false, true, null);
        return delegate.getUnpickData();
    }
}
