package io.github.enbrain.loomlayeredyarn.util;

import java.io.IOException;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;
import net.fabricmc.mappingio.tree.MappingTreeView.ElementMappingView;

public class MappingDiffVisitor extends ForwardingMappingVisitor {
    private final MappingTree base;
    private final int baseNamedId;

    public MappingDiffVisitor(MappingVisitor next, MappingTree base, String matchNs) {
        super(next);
        this.base = base;
        this.baseNamedId = base.getNamespaceId(matchNs);
    }

    private String currentClass = null;
    private String currentFieldName = null;
    private String currentFieldDesc = null;
    private String currentMethodName = null;
    private String currentMethodDesc = null;
    private int currentArgPos = 0;
    private int currentLvIndex = 0;

    @Override
    public boolean visitClass(String srcName) throws IOException {
        this.currentClass = srcName;
        return next.visitClass(srcName);
    }

    @Override
    public boolean visitField(String srcName, String srcDesc) throws IOException {
        this.currentFieldName = srcName;
        this.currentFieldDesc = srcDesc;
        return next.visitField(srcName, srcDesc);
    }

    @Override
    public boolean visitMethod(String srcName, String srcDesc) throws IOException {
        this.currentMethodName = srcName;
        this.currentMethodDesc = srcDesc;
        return next.visitMethod(srcName, srcDesc);
    }

    @Override
    public boolean visitMethodArg(int argPosition, int lvIndex, String srcName) throws IOException {
        this.currentArgPos = argPosition;
        this.currentLvIndex = lvIndex;
        return next.visitMethodArg(argPosition, lvIndex, srcName);
    }

    @Override
    public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
        ElementMappingView element = switch (targetKind) {
            case CLASS -> base.getClass(this.currentClass);
            case FIELD -> base.getField(this.currentClass, this.currentFieldName, this.currentFieldDesc);
            case METHOD -> base.getMethod(this.currentClass, this.currentMethodName,
                    this.currentMethodDesc);
            case METHOD_ARG -> {
                MethodMapping method = base.getMethod(this.currentClass, this.currentMethodName,
                        this.currentMethodDesc);

                yield method == null ? null : method.getArg(this.currentArgPos, this.currentLvIndex, null);
            }
            default -> throw new UnsupportedOperationException("Unsupported targetKind: " + targetKind);
        };

        if (element == null || !element.getComment().equals(comment)) {
            next.visitComment(targetKind, comment);
        }
    }

    @Override
    public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
        ElementMappingView element = switch (targetKind) {
            case CLASS -> base.getClass(this.currentClass);
            case FIELD -> base.getField(this.currentClass, this.currentFieldName, this.currentFieldDesc);
            case METHOD -> base.getMethod(this.currentClass, this.currentMethodName,
                    this.currentMethodDesc);
            case METHOD_ARG -> {
                MethodMapping method = base.getMethod(this.currentClass, this.currentMethodName,
                        this.currentMethodDesc);

                yield method == null ? null : method.getArg(this.currentArgPos, this.currentLvIndex, null);
            }
            default -> throw new UnsupportedOperationException("Unsupported targetKind: " + targetKind);
        };

        if (element == null || !element.getDstName(this.baseNamedId).equals(name)) {
            next.visitDstName(targetKind, namespace, name);
        }
    }
}
