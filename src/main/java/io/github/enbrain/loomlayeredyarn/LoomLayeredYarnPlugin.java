package io.github.enbrain.loomlayeredyarn;

import java.nio.file.Path;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

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

        public MappingsSpec<YarnMappingsLayer> yarn(Object object) {
            return new YarnMappingsSpec(FileSpec.create(object));
        }

        public MappingsSpec<YarnGithubDiffMappingsLayer> githubDiff(String repo, String ref, Object base) {
            return new YarnGithubDiffMappingsSpec(FileSpec.create(new GithubDependency(repo, ref, this.project)),
                    FileSpec.create(base));
        }
    }

    public static Path getCachePath(Project project) {
        return project.getGradle().getGradleUserHomeDir().toPath().resolve("caches").resolve("loom-layered-yarn");
    }
}
