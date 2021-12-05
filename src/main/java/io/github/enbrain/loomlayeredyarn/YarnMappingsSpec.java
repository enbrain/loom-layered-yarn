package io.github.enbrain.loomlayeredyarn;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public record YarnMappingsSpec(FileSpec fileSpec) implements MappingsSpec<YarnMappingsLayer> {
    @Override
    public YarnMappingsLayer createLayer(MappingContext context) {
        return new YarnMappingsLayer(fileSpec.get(context), context.mappingsProvider().intermediaryTinyFile().toPath());
    }
}
