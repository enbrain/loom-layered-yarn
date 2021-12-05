package io.github.enbrain.loomlayeredyarn;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public class LoomLayeredYarnPlugin implements Plugin<Project> {
    public void apply(Project target) {
        target.getExtensions().create("loomLayeredYarn", LoomLayeredYarnExtension.class);
    }

    public static class LoomLayeredYarnExtension {
        public MappingsSpec<YarnMappingsLayer> yarn(Object object) {
            return new YarnMappingsSpec(FileSpec.create(object));
        }
    }
}
