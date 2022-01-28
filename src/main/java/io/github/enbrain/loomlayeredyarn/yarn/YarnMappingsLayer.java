package io.github.enbrain.loomlayeredyarn.yarn;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.enbrain.loomlayeredyarn.util.MappingDiffVisitor;
import io.github.enbrain.loomlayeredyarn.util.MappingHelper;
import io.github.enbrain.loomlayeredyarn.util.MappingValidator;
import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.configuration.providers.mappings.intermediary.IntermediaryMappingLayer;
import net.fabricmc.mappingio.MappingUtil;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public record YarnMappingsLayer(Path sourcePath, @Nullable Path basePath,
        Supplier<MemoryMappingTree> intermediarySupplier)
        implements MappingLayer {

    private static Map<String, String> ENIGMA_NAMESPACE_MAP = Map.of(
            MappingUtil.NS_SOURCE_FALLBACK,
            MappingsNamespace.INTERMEDIARY.toString(),
            MappingUtil.NS_TARGET_FALLBACK,
            MappingsNamespace.NAMED.toString());

    @Override
    public void visit(MappingVisitor dest) throws IOException {
        MappingVisitor validator = new MappingValidator(dest, intermediarySupplier.get());
        MappingVisitor diffVisitor = diff(validator);
        MappingVisitor nsSwitch = new MappingSourceNsSwitch(diffVisitor, getSourceNamespace().toString());
        MappingVisitor nsRenamer = new MappingNsRenamer(nsSwitch, ENIGMA_NAMESPACE_MAP);
        MappingHelper.read(sourcePath, nsRenamer);
    }

    private MappingVisitor diff(MappingVisitor visitor) throws IOException {
        if (basePath != null) {
            MemoryMappingTree base = new MemoryMappingTree();
            MappingVisitor nsSwitch = new MappingSourceNsSwitch(base, getSourceNamespace().toString());
            MappingVisitor nsRenamer = new MappingNsRenamer(nsSwitch, ENIGMA_NAMESPACE_MAP);
            MappingHelper.read(basePath, nsRenamer);
            return new MappingDiffVisitor(visitor, base, MappingsNamespace.NAMED.toString());
        } else {
            return visitor;
        }
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
