package io.github.enbrain.loomlayeredyarn.appendtojavadoc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.enbrain.loomlayeredyarn.appendtojavadoc.AppendToJavadocMappingsSpec.AdditionalMapping;
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

public record AppendToJavadocMappingsLayer(Path basePath, List<AdditionalMapping> additionalMappings) implements MappingLayer {

    private static final String DST_NAMESPACE = MappingsNamespace.NAMED.toString();

    @Override
    public void visit(MappingVisitor mappingVisitor) throws IOException {
        MemoryMappingTree baseTree = this.readMappings(this.basePath);

        Map<ElementMapping, List<String>> additionalComments = new HashMap<>();

        for (AdditionalMapping additional : additionalMappings) {
            MemoryMappingTree additionalTree = this.readMappings(additional.path());

            for (ClassMapping additionalClass : additionalTree.getClasses()) {
                ClassMapping baseClass = baseTree.getClass(additionalClass.getSrcName());

                if (baseClass == null) {
                    baseTree.addClass(additionalClass);
                } else {
                    this.addComment(baseClass, additionalClass, additional.name(), additionalComments);
                }

                for (FieldMapping additionalField : additionalClass.getFields()) {
                    FieldMapping baseField = baseClass.getField(additionalField.getSrcName(), additionalField.getSrcDesc());
                    if (baseField == null) {
                        baseClass.addField(additionalField);
                    } else {
                        this.addComment(baseField, additionalField, additional.name(), additionalComments);
                    }
                }

                for (MethodMapping additionalMethod : additionalClass.getMethods()) {
                    MethodMapping baseMethod = baseClass.getMethod(additionalMethod.getSrcName(), additionalMethod.getSrcDesc());
                    if (baseMethod == null) {
                        baseClass.addMethod(additionalMethod);
                    } else {
                        this.addComment(baseMethod, additionalMethod, additional.name(), additionalComments);
                    }
                }
            }
        }

        additionalComments.entrySet().forEach(AppendToJavadocMappingsLayer::appendComment);

        baseTree.accept(mappingVisitor);
    }

    private void addComment(ElementMapping base, ElementMappingView additional, String additionalMappingsName, Map<ElementMapping, List<String>> additionalComments) {
        String comment = Objects.requireNonNullElse(base.getComment(), "");

        if (this.contributes(base, additional, DST_NAMESPACE, DST_NAMESPACE)) {
            String additionalName = getFullName(additional, DST_NAMESPACE);

            String additionalComment = additionalMappingsName.isEmpty() ? additionalName : "@%s %s".formatted(additionalMappingsName, additionalName);

            additionalComments.computeIfAbsent(base, k -> new ArrayList<>()).add(additionalComment);
        }

        if (!comment.isEmpty()) {
            base.setComment(comment);
        }
    }

    private boolean contributes(ElementMappingView base, ElementMappingView additional, String baseNamespace, String additonalNamespace) {
        return isDifferent(additional, additional, this.getSourceNamespace().toString(), additonalNamespace) && isDifferent(base, additional, baseNamespace, additonalNamespace);
    }

    private static boolean isDifferent(ElementMappingView base, ElementMappingView additional, String baseNamespace, String additonalNamespace) {
        String baseName = base.getName(baseNamespace);
        String additionalName = additional.getName(additonalNamespace);

        boolean different = !baseName.equals(additionalName);

        if (base instanceof MethodMappingView baseMethod && additional instanceof MethodMappingView additionalMethod) {
            for (MethodArgMappingView additionalArg : additionalMethod.getArgs()) {
                MethodArgMappingView baseArg = baseMethod.getArg(additionalArg.getArgPosition(), additionalArg.getLvIndex(), null);

                different |= additionalArg.getName(additonalNamespace) != null && (baseArg == null || !additionalArg.getName(additonalNamespace).equals(baseArg.getName(baseNamespace)));
            }
        }

        return different;
    }

    private static String getFullName(ElementMappingView mapping, String namespace) {
        String name = mapping.getName(namespace);

        if (mapping instanceof MethodMappingView methodMapping) {
            String args = methodMapping.getArgs().stream().sorted(Comparator.comparing(MethodArgMappingView::getLvIndex)).map(arg -> arg.getLvIndex() + ":" + arg.getName(DST_NAMESPACE)).collect(Collectors.joining(", "));
            if (!args.isEmpty()) {
                name += "(" + args + ")";
            }
        }

        return name;
    }

    private MemoryMappingTree readMappings(Path path) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        MappingHelper.read(path, new MappingSourceNsSwitch(tree, this.getSourceNamespace().toString()));
        return tree;
    }

    private static void appendComment(Map.Entry<ElementMapping, List<String>> additionalCommentEntry) {
        ElementMapping baseMapping = additionalCommentEntry.getKey();
        String additionalComment = String.join("\n", additionalCommentEntry.getValue());

        String comment = Objects.requireNonNullElse(baseMapping.getComment(), "");

        while (!comment.isEmpty() && !comment.endsWith("\n\n")) {
            comment += "\n";
        }

        comment += additionalComment;
        baseMapping.setComment(comment);
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
