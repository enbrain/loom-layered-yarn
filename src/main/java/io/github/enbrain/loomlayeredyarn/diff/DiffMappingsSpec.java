package io.github.enbrain.loomlayeredyarn.diff;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public record DiffMappingsSpec(FileSpec head, FileSpec base) implements MappingsSpec<DiffMappingsLayer> {
    @Override
    public DiffMappingsLayer createLayer(MappingContext context) {
        return new DiffMappingsLayer(this.head.get(context), this.base.get(context));
    }
}
