package io.github.enbrain.loomlayeredyarn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.configuration.providers.mappings.intermediary.IntermediaryMappingLayer;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.Tiny2Reader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public record YarnMappingsLayer(Path mappingFile, Path intermediaryFile) implements MappingLayer {
    @Override
    public void visit(MappingVisitor mappingVisitor) throws IOException {
        MemoryMappingTree intermediary = new MemoryMappingTree();

        try (BufferedReader reader = Files.newBufferedReader(intermediaryFile, StandardCharsets.UTF_8)) {
            Tiny2Reader.read(reader, new MappingSourceNsSwitch(intermediary, getSourceNamespace().toString()));
        }

        try (ZipFile zip = new ZipFile(mappingFile.toFile())) {
            ZipEntry entry = zip.getEntry("mappings/mappings.tiny");

            try (Reader reader = new InputStreamReader(zip.getInputStream(entry))) {
                MappingSourceNsSwitch nsSwitch = new MappingSourceNsSwitch(mappingVisitor,
                        getSourceNamespace().toString());

                MappingVisitor validation = new ForwardingMappingVisitor(nsSwitch) {
                    @Override
                    public boolean visitClass(String srcName) throws IOException {
                        if (intermediary.getClass(srcName) != null) {
                            return next.visitClass(srcName);
                        } else {
                            return false;
                        }
                    }
                };

                MappingReader.read(reader, null, validation);
            }
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
