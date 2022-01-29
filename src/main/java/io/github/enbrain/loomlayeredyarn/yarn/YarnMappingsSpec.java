package io.github.enbrain.loomlayeredyarn.yarn;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public record YarnMappingsSpec(FileSpec source) implements MappingsSpec<YarnMappingsLayer> {
    @Override
    public YarnMappingsLayer createLayer(MappingContext context) {
        return new YarnMappingsLayer(source.get(context), context.intermediaryTree());
    }
}
