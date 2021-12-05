package io.github.enbrain.loomlayeredyarn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.configuration.providers.mappings.intermediary.IntermediaryMappingLayer;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingUtil;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.EnigmaReader;
import net.fabricmc.mappingio.format.Tiny2Reader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public record YarnGithubDiffMappingsLayer(Path mappingsFile, Path baseFile, Path intermediaryFile)
        implements MappingLayer {
    @Override
    public void visit(MappingVisitor mappingVisitor) throws IOException {
        MemoryMappingTree intermediary = new MemoryMappingTree();

        try (BufferedReader reader = Files.newBufferedReader(intermediaryFile,
                StandardCharsets.UTF_8)) {
            Tiny2Reader.read(reader, new MappingSourceNsSwitch(intermediary,
                    getSourceNamespace().toString()));
        }

        MemoryMappingTree base = new MemoryMappingTree();

        try (ZipFile zip = new ZipFile(baseFile.toFile())) {
            ZipEntry entry = zip.getEntry("mappings/mappings.tiny");

            try (Reader reader = new InputStreamReader(zip.getInputStream(entry))) {
                MappingReader.read(reader, null, new MappingSourceNsSwitch(base,
                        getSourceNamespace().toString()));
            }
        }

        try (FileSystem fs = FileSystems.newFileSystem(mappingsFile, (ClassLoader) null)) {
            MappingVisitor diff = new MappingDiffVisitor(mappingVisitor, base);

            MappingVisitor validation = new MappingValidator(diff, intermediary);

            MappingVisitor nsRename = new MappingNsRenamer(validation,
                    Map.of(MappingUtil.NS_SOURCE_FALLBACK, "intermediary", MappingUtil.NS_TARGET_FALLBACK, "named"));

            EnigmaReader.read(fs.getPath("/"), nsRename);
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
