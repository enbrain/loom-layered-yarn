package io.github.enbrain.loomlayeredyarn;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public record YarnMappingsSpec(FileSpec source, @Nullable FileSpec base) implements MappingsSpec<YarnMappingsLayer> {
    @Override
    public YarnMappingsLayer createLayer(MappingContext context) {
        return new YarnMappingsLayer(source.get(context), base == null ? null : base.get(context),
                context.mappingsProvider().intermediaryTinyFile().toPath());
    }
}
