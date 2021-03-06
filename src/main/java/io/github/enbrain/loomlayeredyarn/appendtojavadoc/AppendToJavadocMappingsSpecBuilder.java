package io.github.enbrain.loomlayeredyarn.appendtojavadoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingsDependency;

public class AppendToJavadocMappingsSpecBuilder {
    private @Nullable FileSpec base;
    private List<AdditionalMappingSpec> additionalEntries = new ArrayList<>();

    private AppendToJavadocMappingsSpecBuilder() {
    }

    public static AppendToJavadocMappingsSpecBuilder builder() {
        return new AppendToJavadocMappingsSpecBuilder();
    }

    public AppendToJavadocMappingsSpecBuilder setBase(LayeredMappingsDependency base) {
        this.base = FileSpec.create(base);
        return this;
    }

    public AppendToJavadocMappingsSpecBuilder add(String name, LayeredMappingsDependency mapping) {
        this.additionalEntries.add(new AdditionalMappingSpec(name, FileSpec.create(mapping)));
        return this;
    }

    public AppendToJavadocMappingsSpec build() {
        Objects.requireNonNull(this.base, "base is not set");

        return new AppendToJavadocMappingsSpec(this.base, additionalEntries);
    }

    public static record AdditionalMappingSpec(String prefix, FileSpec fileSpec) {
    }
}
