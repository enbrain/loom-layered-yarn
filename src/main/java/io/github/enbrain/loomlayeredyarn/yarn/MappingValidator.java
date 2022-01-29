package io.github.enbrain.loomlayeredyarn.yarn;

import java.io.IOException;

import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;

public class MappingValidator extends ForwardingMappingVisitor {
    private final MappingTree intermediary;
    private final int intermediaryNamespace;

    public MappingValidator(MappingVisitor next, MappingTree intermediary) {
        super(next);
        this.intermediary = intermediary;
        this.intermediaryNamespace = intermediary.getNamespaceId(MappingsNamespace.INTERMEDIARY.toString());
    }

    @Override
    public boolean visitClass(String srcName) throws IOException {
        if (intermediary.getClass(srcName, intermediaryNamespace) != null) {
            return super.visitClass(srcName);
        } else {
            return false;
        }
    }
}
