package io.github.enbrain.loomlayeredyarn;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;

public class YarnMappingsSpecBuilder {
    private final FileSpec source;
    private @Nullable Object base = null;

    private YarnMappingsSpecBuilder(FileSpec source) {
        this.source = source;
    }

    public static YarnMappingsSpecBuilder builder(FileSpec source) {
        return new YarnMappingsSpecBuilder(source);
    }

    public YarnMappingsSpecBuilder setBase(Object base) {
        this.base = base;
        return this;
    }

    public YarnMappingsSpec build() {
        return new YarnMappingsSpec(this.source, this.base == null ? null : FileSpec.create(this.base));
    }
}
