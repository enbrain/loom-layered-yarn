package io.github.enbrain.loomlayeredyarn;

import java.nio.file.Path;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public class LoomLayeredYarnPlugin implements Plugin<Project> {
    public void apply(Project target) {
        target.getExtensions().create("loomLayeredYarn", LoomLayeredYarnExtension.class, target);
    }

    public static class LoomLayeredYarnExtension {
        private final Project project;

        public LoomLayeredYarnExtension(Project project) {
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
            YarnMappingsSpecBuilder builder = YarnMappingsSpecBuilder.builder(FileSpec.create(source));
            action.execute(builder);
            return builder.build();
        }

        public Dependency github(String repo, String ref) {
            return new GithubDependency(repo, ref, this.project);
        }
    }

    public static Path getCachePath(Project project) {
        return project.getGradle().getGradleUserHomeDir().toPath().resolve("caches").resolve("loom-layered-yarn");
    }
}
