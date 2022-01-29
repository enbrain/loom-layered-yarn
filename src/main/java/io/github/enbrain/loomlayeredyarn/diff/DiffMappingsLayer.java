package io.github.enbrain.loomlayeredyarn.diff;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import io.github.enbrain.loomlayeredyarn.util.MappingHelper;
import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.configuration.providers.mappings.intermediary.IntermediaryMappingLayer;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public record DiffMappingsLayer(Path head, Path base) implements MappingLayer {
    @Override
    public void visit(MappingVisitor mappingVisitor) throws IOException {
        MemoryMappingTree baseTree = new MemoryMappingTree();
        read(this.base, baseTree);
        read(this.head, new MappingDiffVisitor(mappingVisitor, baseTree, MappingsNamespace.NAMED.toString()));
    }

    private static void read(Path path, MappingVisitor visitor) throws IOException {
        MappingHelper.read(path, new MappingSourceNsSwitch(visitor, MappingsNamespace.INTERMEDIARY.toString()));
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
