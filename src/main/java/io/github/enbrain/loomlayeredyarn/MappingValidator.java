package io.github.enbrain.loomlayeredyarn;

import java.io.IOException;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;

public class MappingValidator extends ForwardingMappingVisitor {
    private final MappingTree intermediary;

    public MappingValidator(MappingVisitor next, MappingTree intermediary) {
        super(next);
        this.intermediary = intermediary;
    }

    @Override
    public boolean visitClass(String srcName) throws IOException {
        if (intermediary.getClass(srcName) != null) {
            return next.visitClass(srcName);
        } else {
            return false;
        }
    }
}
