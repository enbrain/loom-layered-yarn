package io.github.enbrain.loomlayeredyarn.unpick;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskDependency;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingSpec;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingsDependency;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingsProcessor;

public class UnpickEnabledDependency implements FileCollectionDependency {
    private static final String MAPPINGS_CONSTANTS = net.fabricmc.loom.util.Constants.Configurations.MAPPING_CONSTANTS;

    private static final Field MAPPING_CONTEXT_FIELD;
    private static final Field LAYERED_MAPPING_SPEC_FIELD;

    private static final Method WRITE_MAPPING_METHOD;
    private static final Method WRITE_SIGNATURE_FIXES_METHOD;

    static {
        try {
            MAPPING_CONTEXT_FIELD = LayeredMappingsDependency.class.getDeclaredField("mappingContext");
            MAPPING_CONTEXT_FIELD.setAccessible(true);

            LAYERED_MAPPING_SPEC_FIELD = LayeredMappingsDependency.class.getDeclaredField("layeredMappingSpec");
            LAYERED_MAPPING_SPEC_FIELD.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }

        try {
            WRITE_MAPPING_METHOD = LayeredMappingsDependency.class.getDeclaredMethod("writeMapping", LayeredMappingsProcessor.class, List.class, Path.class);
            WRITE_MAPPING_METHOD.setAccessible(true);

            WRITE_SIGNATURE_FIXES_METHOD = LayeredMappingsDependency.class.getDeclaredMethod("writeSignatureFixes", LayeredMappingsProcessor.class, List.class, Path.class);
            WRITE_SIGNATURE_FIXES_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
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
        this.fixConstantsDependencies();
        return layeredMappingsDependency.resolve();
    }

    private void fixConstantsDependencies() {
        MappingContext mappingContext;
        LayeredMappingSpec layeredMappingSpec;

        try {
            mappingContext = (MappingContext) MAPPING_CONTEXT_FIELD.get(layeredMappingsDependency);
            layeredMappingSpec = (LayeredMappingSpec) LAYERED_MAPPING_SPEC_FIELD.get(layeredMappingsDependency);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        LayeredMappingsProcessor processor = new LayeredMappingsProcessor(layeredMappingSpec);
        List<MappingLayer> layers = processor.resolveLayers(mappingContext);

        UnpickLayerImpl lastUnpickLayer = null;

        for (MappingLayer layer : layers) {
            if (layer instanceof UnpickLayerImpl unpickLayer) {
                lastUnpickLayer = unpickLayer;
            }
        }

        if (lastUnpickLayer != null) {
            String constantsDependency = lastUnpickLayer.constantsDependency();

            this.project.getConfigurations().getByName(MAPPINGS_CONSTANTS).withDependencies(dependencies -> {
                dependencies.removeIf(dep -> dep.getGroup().equals("loom") && dep.getName().equals("mappings"));
                dependencies.add(this.project.getDependencies().create(constantsDependency));
            });
        }
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
