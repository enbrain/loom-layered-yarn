package io.github.enbrain.loomlayeredyarn.yarn;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import io.github.enbrain.loomlayeredyarn.util.MappingHelper;
import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.configuration.providers.mappings.file.FileMappingsLayer;
import net.fabricmc.loom.configuration.providers.mappings.intermediary.IntermediaryMappingLayer;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public record YarnMappingsLayer(Path source, Supplier<MemoryMappingTree> intermediary) implements MappingLayer {

    @Override
    public void visit(MappingVisitor mappingVisitor) throws IOException {
        MappingHelper.findMappingPath(source, p -> {
            try {
                FileMappingsLayer fileMappingsLayer = new FileMappingsLayer(p, "", MappingsNamespace.INTERMEDIARY.toString(), MappingsNamespace.NAMED.toString(), false, this.getSourceNamespace().toString());
                fileMappingsLayer.visit(new MappingValidator(mappingVisitor, this.intermediary.get()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public MappingsNamespace getSourceNamespace() {
        return MappingsNamespace.INTERMEDIARY;
    }

    @Override
    public List<Class<? extends MappingLayer>> dependsOn() {
        return List.of(IntermediaryMappingLayer.class);
    }
}
