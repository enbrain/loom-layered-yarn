package io.github.enbrain.loomlayeredyarn;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

import io.github.enbrain.loomlayeredyarn.appendtojavadoc.AppendToJavadocMappingsLayer;
import io.github.enbrain.loomlayeredyarn.appendtojavadoc.AppendToJavadocMappingsSpecBuilder;
import io.github.enbrain.loomlayeredyarn.diff.DiffMappingsLayer;
import io.github.enbrain.loomlayeredyarn.diff.DiffMappingsSpecBuilder;
import io.github.enbrain.loomlayeredyarn.unpick.UnpickEnabledDependency;
import io.github.enbrain.loomlayeredyarn.unpick.UnpickLayer;
import io.github.enbrain.loomlayeredyarn.unpick.UnpickSpec;
import io.github.enbrain.loomlayeredyarn.util.GithubDependency;
import io.github.enbrain.loomlayeredyarn.util.LocalDirectorySpec;
import io.github.enbrain.loomlayeredyarn.yarn.YarnMappingsLayer;
import io.github.enbrain.loomlayeredyarn.yarn.YarnMappingsSpec;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingsDependency;

public class LayeredYarnExtension {
    private final Project project;

    public LayeredYarnExtension(Project project) {
        this.project = project;
    }

    public MappingsSpec<YarnMappingsLayer> yarn(Object source) {
        return new YarnMappingsSpec(createFileSpec(source));
    }

    public MappingsSpec<AppendToJavadocMappingsLayer> appendToJavadoc(Action<AppendToJavadocMappingsSpecBuilder> action) {
        AppendToJavadocMappingsSpecBuilder builder = AppendToJavadocMappingsSpecBuilder.builder();
        action.execute(builder);
        return builder.build();
    }

    public Dependency github(String repo, String ref) {
        return GithubDependency.of(repo, ref, this.project);
    }

    public MappingsSpec<DiffMappingsLayer> pr(String repo, int number) {
        LoomGradleExtensionAPI loom = this.project.getExtensions().getByType(LoomGradleExtensionAPI.class);
        return diff(diffBuilder -> {
            JsonObject info = GithubDependency.getPrInfo(repo, number);
            diffBuilder.setHead((LayeredMappingsDependency) loom.layered(builder -> {
                builder.addLayer(yarn(GithubDependency.fromPrMergeCommit(repo, info, this.project)));
            }));
            diffBuilder.setBase((LayeredMappingsDependency) loom.layered(builder -> {
                builder.addLayer(yarn(GithubDependency.fromPrBaseRef(repo, info, this.project)));
            }));
        });
    }

    public MappingsSpec<DiffMappingsLayer> diff(Action<DiffMappingsSpecBuilder> action) {
        DiffMappingsSpecBuilder builder = DiffMappingsSpecBuilder.builder();
        action.execute(builder);
        return builder.build();
    }

    public MappingsSpec<UnpickLayer> unpick(String dependencyNotation) {
        Dependency dependency = this.project.getDependencies().create(dependencyNotation);
        String constantsDependency = "%s:%s:%s:constants".formatted(dependency.getGroup(), dependency.getName(), dependency.getVersion());
        return new UnpickSpec(dependencyNotation, constantsDependency);
    }

    public Dependency enableUnpick(Dependency dependency) {
        if (dependency instanceof LayeredMappingsDependency layeredMappingsDependency) {
            return new UnpickEnabledDependency(layeredMappingsDependency, this.project);
        } else {
            throw new IllegalArgumentException("dependency is not LayeredMappingsDependency");
        }
    }

    private static FileSpec createFileSpec(Object object) {
        if (object instanceof Path path && Files.isDirectory(path)) {
            return new LocalDirectorySpec(path);
        } else if (object instanceof File file && file.isDirectory()) {
            return new LocalDirectorySpec(file.toPath());
        } else {
            return FileSpec.create(object);
        }
    }
}
