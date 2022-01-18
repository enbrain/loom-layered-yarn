package io.github.enbrain.loomlayeredyarn.unpick;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskDependency;

import net.fabricmc.loom.LoomGradlePlugin;
import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingSpec;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingsDependency;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingsProcessor;
import net.fabricmc.loom.util.ZipUtils;

public class UnpickEnabledDependency implements FileCollectionDependency {
    private static final Field MAPPING_CONTEXT_FIELD;
    private static final Field LAYERED_MAPPING_SPEC_FIELD;

    static {
        try {
            MAPPING_CONTEXT_FIELD = LayeredMappingsDependency.class.getDeclaredField("mappingContext");
            MAPPING_CONTEXT_FIELD.setAccessible(true);

            LAYERED_MAPPING_SPEC_FIELD = LayeredMappingsDependency.class.getDeclaredField("layeredMappingSpec");
            LAYERED_MAPPING_SPEC_FIELD.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private LayeredMappingsDependency layeredMappingsDependency;
    private Project project;

    public UnpickEnabledDependency(LayeredMappingsDependency layeredMappingsDependency, Project project) {
        this.layeredMappingsDependency = layeredMappingsDependency;
        this.project = project;
    }

    @Override
    public Set<File> resolve() {
        MappingContext mappingContext;
        LayeredMappingSpec layeredMappingSpec;

        try {
            mappingContext = (MappingContext) MAPPING_CONTEXT_FIELD.get(layeredMappingsDependency);
            layeredMappingSpec = (LayeredMappingSpec) LAYERED_MAPPING_SPEC_FIELD.get(layeredMappingsDependency);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Path mappingsDir = mappingContext.minecraftProvider().dir("layered").toPath();
        Path mappingsFile = mappingsDir.resolve("loom.mappings-%s.tiny".formatted(this.getVersion()));

        boolean writesUnpick = !Files.exists(mappingsFile) || LoomGradlePlugin.refreshDeps;

        Set<File> files = layeredMappingsDependency.resolve();

        LayeredMappingsProcessor processor = new LayeredMappingsProcessor(layeredMappingSpec);
        List<MappingLayer> layers = processor.resolveLayers(mappingContext);

        UnpickLayer lastUnpickLayer = null;

        for (MappingLayer layer : layers) {
            if (layer instanceof UnpickLayer unpickLayer) {
                lastUnpickLayer = unpickLayer;
            }
        }

        if (lastUnpickLayer != null) {
            if (writesUnpick) {
                try {
                    ZipUtils.add(mappingsFile, UnpickLayer.UNPICK_DEFINITION_PATH,
                            lastUnpickLayer.getUnpickDefinition());
                    ZipUtils.add(mappingsFile, UnpickLayer.UNPICK_METADATA_PATH, lastUnpickLayer.getUnpickMetadata());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String constantsDependency = lastUnpickLayer.getConstantsDependency();

            this.project.getConfigurations().getByName("mappingsConstants").withDependencies(dependencies -> {
                dependencies.removeIf(dep -> dep.getGroup().equals("loom") && dep.getName().equals("mappings"));
                dependencies.add(this.project.getDependencies().create(constantsDependency));
            });
        }

        return files;
    }

    @Override
    public Set<File> resolve(boolean transitive) {
        return this.resolve();
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return layeredMappingsDependency.getBuildDependencies();
    }

    @Override
    public String getGroup() {
        return layeredMappingsDependency.getGroup();
    }

    @Override
    public String getName() {
        return layeredMappingsDependency.getName();
    }

    @Override
    public String getVersion() {
        return layeredMappingsDependency.getVersion();
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        return layeredMappingsDependency.contentEquals(dependency);
    }

    @Override
    public Dependency copy() {
        return new UnpickEnabledDependency(layeredMappingsDependency, project);
    }

    @Override
    public String getReason() {
        return layeredMappingsDependency.getReason();
    }

    @Override
    public void because(String s) {
        layeredMappingsDependency.because(s);
    }

    @Override
    public FileCollection getFiles() {
        return layeredMappingsDependency.getFiles();
    }
}
