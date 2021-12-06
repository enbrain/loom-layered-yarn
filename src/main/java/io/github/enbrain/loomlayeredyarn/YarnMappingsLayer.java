package io.github.enbrain.loomlayeredyarn;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.configuration.providers.mappings.intermediary.IntermediaryMappingLayer;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingUtil;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public record YarnMappingsLayer(Path sourcePath, @Nullable Path basePath, Path intermediaryFile)
        implements MappingLayer {

    private static Map<String, String> ENIGMA_NAMESPACE_MAP = Map.of(
            MappingUtil.NS_SOURCE_FALLBACK,
            MappingsNamespace.INTERMEDIARY.toString(),
            MappingUtil.NS_TARGET_FALLBACK,
            MappingsNamespace.NAMED.toString());

    @Override
    public void visit(MappingVisitor dest) throws IOException {
        MemoryMappingTree intermediary = new MemoryMappingTree();
        MappingReader.read(intermediaryFile, new MappingSourceNsSwitch(intermediary, getSourceNamespace().toString()));

        MappingVisitor validator = new MappingValidator(dest, intermediary);
        MappingVisitor diffVisitor = diff(validator);
        MappingVisitor nsSwitch = new MappingSourceNsSwitch(diffVisitor, getSourceNamespace().toString());
        MappingVisitor nsRenamer = new MappingNsRenamer(nsSwitch, ENIGMA_NAMESPACE_MAP);
        read(sourcePath, nsRenamer);
    }

    private MappingVisitor diff(MappingVisitor visitor) throws IOException {
        if (basePath != null) {
            MemoryMappingTree base = new MemoryMappingTree();
            MappingVisitor nsSwitch = new MappingSourceNsSwitch(base, getSourceNamespace().toString());
            MappingVisitor nsRenamer = new MappingNsRenamer(nsSwitch, ENIGMA_NAMESPACE_MAP);
            read(basePath, nsRenamer);
            return new MappingDiffVisitor(visitor, base, MappingsNamespace.NAMED.toString());
        } else {
            return visitor;
        }
    }

    private static void read(Path path, MappingVisitor visitor) throws IOException {
        if (isZip(path)) {
            try (FileSystem fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
                Path p = fs.getPath("mappings/mappings.tiny");
                if (!Files.exists(p)) {
                    p = fs.getPath("/");
                }

                MappingReader.read(p, visitor);
            }
        } else {
            MappingReader.read(path, visitor);
        }
    }

    private static boolean isZip(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            return false;
        }

        try (Reader reader = new InputStreamReader(Files.newInputStream(path))) {
            int c1 = reader.read();
            int c2 = reader.read();
            int c3 = reader.read();
            int c4 = reader.read();

            if ((c1 | c2 | c3 | c4) < 0) {
                return false;
            }

            int signature = (c1 << 24) + (c2 << 16) + (c3 << 8) + (c4 << 0);
            return signature == 0x504B0304 || signature == 0x504B0506 || signature == 0x504B0708;
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
