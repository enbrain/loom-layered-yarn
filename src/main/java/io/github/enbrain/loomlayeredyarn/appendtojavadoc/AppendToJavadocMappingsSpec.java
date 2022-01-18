package io.github.enbrain.loomlayeredyarn.appendtojavadoc;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public record AppendToJavadocMappingsSpec(FileSpec base,
        List<AppendToJavadocMappingsSpecBuilder.AdditionalMappingSpec> additionalSpecs)
        implements MappingsSpec<AppendToJavadocMappingsLayer> {

    @Override
    public AppendToJavadocMappingsLayer createLayer(MappingContext context) {
        List<AdditionalMapping> additionalMappings = additionalSpecs.stream()
                .map(additional -> new AdditionalMapping(additional.prefix(), additional.fileSpec().get(context)))
                .collect(Collectors.toList());

        return new AppendToJavadocMappingsLayer(base.get(context), additionalMappings);
    }

    public static record AdditionalMapping(String name, Path path) {
    }
}
