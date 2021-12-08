package io.github.enbrain.loomlayeredyarn.appendtojavadoc;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;

public class AppendToJavadocMappingsSpecBuilder {
    private @Nullable Object base;
    private @Nullable Object additional;
    private String prefix = "@additionalMapping";

    private AppendToJavadocMappingsSpecBuilder() {
    }

    public static AppendToJavadocMappingsSpecBuilder builder() {
        return new AppendToJavadocMappingsSpecBuilder();
    }

    public AppendToJavadocMappingsSpecBuilder setSource1(Object base) {
        this.base = base;
        return this;
    }

    public AppendToJavadocMappingsSpecBuilder setAdditional(Object additional) {
        this.additional = additional;
        return this;
    }

    public AppendToJavadocMappingsSpecBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public AppendToJavadocMappingsSpec build() {
        Objects.requireNonNull(base, "base is not set");
        Objects.requireNonNull(additional, "additional is not set");

        return new AppendToJavadocMappingsSpec(FileSpec.create(this.base), FileSpec.create(this.additional), prefix);
    }
}
