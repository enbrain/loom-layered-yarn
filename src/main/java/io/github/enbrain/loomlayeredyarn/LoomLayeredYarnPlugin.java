package io.github.enbrain.loomlayeredyarn;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import io.github.enbrain.loomlayeredyarn.appendtojavadoc.AppendToJavadocMappingsLayer;
import io.github.enbrain.loomlayeredyarn.appendtojavadoc.AppendToJavadocMappingsSpecBuilder;
import io.github.enbrain.loomlayeredyarn.util.GithubDependency;
import io.github.enbrain.loomlayeredyarn.util.LocalDirectorySpec;
import io.github.enbrain.loomlayeredyarn.yarn.YarnMappingsLayer;
import io.github.enbrain.loomlayeredyarn.yarn.YarnMappingsSpecBuilder;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public class LoomLayeredYarnPlugin implements Plugin<Project> {
    public void apply(Project target) {
        target.getExtensions().create("layeredYarn", LayeredYarnExtension.class, target);
    }

    public static class LayeredYarnExtension {
        private final Project project;

        public LayeredYarnExtension(Project project) {
            this.project = project;
        }

        public MappingsSpec<YarnMappingsLayer> yarn(Object source) {
            return yarn(source, builder -> {
            });
        }

        @SuppressWarnings({ "rawtypes", "deprecation" })
        public MappingsSpec<YarnMappingsLayer> yarn(Object source,
                @DelegatesTo(value = YarnMappingsSpecBuilder.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
            return yarn(source, builder -> org.gradle.util.ConfigureUtil.configure(closure, builder));
        }

        public MappingsSpec<YarnMappingsLayer> yarn(Object source, Action<YarnMappingsSpecBuilder> action) {
            YarnMappingsSpecBuilder builder = YarnMappingsSpecBuilder.builder(createFileSpec(source));
            action.execute(builder);
            return builder.build();
        }

        @SuppressWarnings({ "rawtypes", "deprecation" })
        public MappingsSpec<AppendToJavadocMappingsLayer> appendToJavadoc(
                @DelegatesTo(value = YarnMappingsSpecBuilder.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
            return appendToJavadoc(builder -> org.gradle.util.ConfigureUtil.configure(closure, builder));
        }

        public MappingsSpec<AppendToJavadocMappingsLayer> appendToJavadoc(
                Action<AppendToJavadocMappingsSpecBuilder> action) {
            AppendToJavadocMappingsSpecBuilder builder = AppendToJavadocMappingsSpecBuilder.builder();
            action.execute(builder);
            return builder.build();
        }

        public Dependency github(String repo, String ref) {
            return new GithubDependency(repo, ref, this.project);
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

    public static Path getCachePath(Project project) {
        return project.getGradle().getGradleUserHomeDir().toPath().resolve("caches").resolve("loom-layered-yarn");
    }
}
