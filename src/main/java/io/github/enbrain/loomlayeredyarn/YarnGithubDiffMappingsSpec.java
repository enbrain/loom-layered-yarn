package io.github.enbrain.loomlayeredyarn;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.FileSpec;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;

public record YarnGithubDiffMappingsSpec(FileSpec mappings, FileSpec base)
        implements MappingsSpec<YarnGithubDiffMappingsLayer> {
    @Override
    public YarnGithubDiffMappingsLayer createLayer(MappingContext context) {
        return new YarnGithubDiffMappingsLayer(mappings.get(context), base.get(context),
                context.mappingsProvider().intermediaryTinyFile().toPath());
    }
}
