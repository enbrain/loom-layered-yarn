package io.github.enbrain.loomlayeredyarn;

import java.nio.file.Path;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import net.fabricmc.loom.LoomGradleExtension;

public class LoomLayeredYarnPlugin implements Plugin<Project> {
    public void apply(Project target) {
        target.getExtensions().create("layeredYarn", LayeredYarnExtension.class, target);

        target.afterEvaluate(project -> {
            try {
                String mappingsIdentifier = LoomGradleExtension.get(project).getMappingsProvider().mappingsIdentifier();
                target.getLogger().lifecycle("Mappings Identifier: " + mappingsIdentifier);
            } catch (NullPointerException e) {
                // Cannot get MappingsProvider before it has been setup
            }
        });
    }

    public static Path getCachePath(Project project) {
        return project.getGradle().getGradleUserHomeDir().toPath().resolve("caches").resolve("loom-layered-yarn");
    }
}
