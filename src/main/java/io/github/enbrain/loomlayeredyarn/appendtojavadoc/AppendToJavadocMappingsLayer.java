package io.github.enbrain.loomlayeredyarn.appendtojavadoc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.enbrain.loomlayeredyarn.util.MappingHelper;
import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.configuration.providers.mappings.intermediary.IntermediaryMappingLayer;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.ElementMapping;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;
import net.fabricmc.mappingio.tree.MappingTreeView.ElementMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.MethodArgMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.MethodMappingView;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public record AppendToJavadocMappingsLayer(Path basePath, Path additionalPath, String prefix) implements MappingLayer {

    private static String DST_NAMESPACE = MappingsNamespace.NAMED.toString();

    @Override
    public void visit(MappingVisitor mappingVisitor) throws IOException {
        MemoryMappingTree baseTree = new MemoryMappingTree();
        MappingHelper.read(basePath, new MappingSourceNsSwitch(baseTree, getSourceNamespace().toString()));

        MemoryMappingTree additionalTree = new MemoryMappingTree();
        MappingHelper.read(additionalPath, new MappingSourceNsSwitch(additionalTree, getSourceNamespace().toString()));

        for (ClassMapping additionalClass : additionalTree.getClasses()) {
            ClassMapping baseClass = baseTree.getClass(additionalClass.getSrcName());
            if (baseClass == null) {
                baseTree.addClass(additionalClass);
            } else {
                this.appendToJavadoc(baseClass, additionalClass);
            }

            for (FieldMapping additionalField : additionalClass.getFields()) {
                FieldMapping baseField = baseClass.getField(additionalField.getSrcName(), additionalField.getSrcDesc());
                if (baseField == null) {
                    baseClass.addField(additionalField);
                } else {
                    this.appendToJavadoc(baseField, additionalField);
                }
            }

            for (MethodMapping additionalMethod : additionalClass.getMethods()) {
                MethodMapping baseMethod = baseClass.getMethod(additionalMethod.getSrcName(),
                        additionalMethod.getSrcDesc());
                if (baseMethod == null) {
                    baseClass.addMethod(additionalMethod);
                } else {
                    this.appendToJavadoc(baseMethod, additionalMethod);
                }
            }
        }

        baseTree.accept(mappingVisitor);
    }

    private void appendToJavadoc(ElementMapping base, ElementMappingView additional) {
        String comment = Objects.requireNonNullElse(base.getComment(), "");

        // TODO: comment merging

        if (additionalContributes(additional, additional, getSourceNamespace().toString(), DST_NAMESPACE) &&
                additionalContributes(base, additional, DST_NAMESPACE, DST_NAMESPACE)) {
            String baseName = getName(base, DST_NAMESPACE);
            String additionalName = getName(additional, DST_NAMESPACE);

            if (!baseName.equals(additionalName)) {
                while (!comment.isEmpty() && !comment.endsWith("\n\n")) {
                    comment += "\n";
                }
                comment += (prefix.isEmpty() ? "" : prefix + " ") + additionalName;
            }
        }

        if (!comment.isEmpty()) {
            base.setComment(comment);
        }
    }

    private static boolean additionalContributes(ElementMappingView base, ElementMappingView additional,
            String baseNamespace, String additonalNamespace) {
        String baseName = base.getName(baseNamespace);
        String additionalName = additional.getName(additonalNamespace);

        boolean contributes = !baseName.equals(additionalName);

        if (base instanceof MethodMappingView baseMethod && additional instanceof MethodMappingView additionalMethod) {
            for (MethodArgMappingView additionalArg : additionalMethod.getArgs()) {
                MethodArgMappingView baseArg = baseMethod.getArg(additionalArg.getArgPosition(),
                        additionalArg.getLvIndex(), null);
                contributes |= baseArg == null
                        || !additionalArg.getName(additonalNamespace).equals(baseArg.getName(baseNamespace));
            }
        }

        return contributes;
    }

    private static String getName(ElementMappingView mapping, String namespace) {
        String name = mapping.getName(namespace);

        if (mapping instanceof MethodMappingView methodMapping) {
            String args = methodMapping.getArgs().stream()
                    .sorted(Comparator.comparing(MethodArgMappingView::getLvIndex))
                    .map(arg -> arg.getLvIndex() + ":" + arg.getName(DST_NAMESPACE))
                    .collect(Collectors.joining(", "));
            if (!args.isEmpty()) {
                name += "(" + args + ")";
            }
        }

        return name;
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
