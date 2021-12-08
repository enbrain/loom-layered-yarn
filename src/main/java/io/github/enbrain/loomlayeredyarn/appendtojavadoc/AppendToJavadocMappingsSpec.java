package io.github.enbrain.loomlayeredyarn.appendtojavadoc;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public record AppendToJavadocMappingsSpec(FileSpec base, FileSpec additional, String prefix)
        implements MappingsSpec<AppendToJavadocMappingsLayer> {
    @Override
    public AppendToJavadocMappingsLayer createLayer(MappingContext context) {
        return new AppendToJavadocMappingsLayer(base.get(context), additional.get(context), prefix);
    }
}
