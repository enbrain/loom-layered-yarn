package io.github.enbrain.loomlayeredyarn.diff;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingsDependency;

public class DiffMappingsSpecBuilder {
    private @Nullable LayeredMappingsDependency head;
    private @Nullable LayeredMappingsDependency base;

    public static DiffMappingsSpecBuilder builder() {
        return new DiffMappingsSpecBuilder();
    }

    public DiffMappingsSpecBuilder setHead(LayeredMappingsDependency head) {
        this.head = head;
        return this;
    }

    public DiffMappingsSpecBuilder setBase(LayeredMappingsDependency base) {
        this.base = base;
        return this;
    }

    public DiffMappingsSpec build() {
        Objects.requireNonNull(this.head, "head is not set");
        Objects.requireNonNull(this.base, "base is not set");

        return new DiffMappingsSpec(FileSpec.create(head), FileSpec.create(base));
    }
}
