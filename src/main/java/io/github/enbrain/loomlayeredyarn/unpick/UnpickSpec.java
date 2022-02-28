package io.github.enbrain.loomlayeredyarn.unpick;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public record UnpickSpec(String dependencyNotation, String constantsDependency) implements MappingsSpec<UnpickLayerImpl> {
    @Override
    public UnpickLayerImpl createLayer(MappingContext context) {
        return new UnpickLayerImpl(FileSpec.create(dependencyNotation).get(context), constantsDependency);
    }

    @Override
    public int hashCode() {
        return dependencyNotation.hashCode();
    }
}
