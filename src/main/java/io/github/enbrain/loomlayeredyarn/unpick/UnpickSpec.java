package io.github.enbrain.loomlayeredyarn.unpick;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public record UnpickSpec(FileSpec source) implements MappingsSpec<UnpickLayer> {
    @Override
    public UnpickLayer createLayer(MappingContext context) {
        return new UnpickLayer(source.get(context));
    }
}
