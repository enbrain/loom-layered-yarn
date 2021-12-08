package io.github.enbrain.loomlayeredyarn.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskDependency;

import io.github.enbrain.loomlayeredyarn.LoomLayeredYarnPlugin;
import net.fabricmc.loom.util.DownloadUtil;

public class GithubDependency implements FileCollectionDependency {
    private String reason;
    private final String owner;
    private final String name;
    private final String ref;
    private final Project project;
    private final Path destination;

    public GithubDependency(String repo, String ref, Project project) {
        String[] p = repo.split("/");
        this.owner = p[0];
        this.name = p[1];
        this.ref = ref;

        this.project = project;

        this.destination = LoomLayeredYarnPlugin.getCachePath(project).resolve("yarn-github")
                .resolve(repo.replace('/', '-') + "-" + ref + ".zip");
    }

    @Override
    public Set<File> resolve() {
        URL url = this.getDownloadUrl();
        try {
            DownloadUtil.downloadIfChanged(url, this.destination.toFile(), this.project.getLogger(), true);
            return Collections.singleton(this.destination.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to download " + url, e);
        }
    }

    private URL getDownloadUrl() {
        String url = "https://api.github.com/repos/" + this.owner + "/" + this.name + "/zipball/" + this.ref;
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Malformed URL: " + url, e);
        }
    }

    @Override
    public Set<File> resolve(boolean transitive) {
        return resolve();
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return task -> Collections.emptySet();
    }

    @Override
    public FileCollection getFiles() {
        this.resolve();
        return this.project.files(this.destination);
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        if (dependency == this) {
            return true;
        } else if (!(dependency instanceof GithubDependency)) {
            return false;
        } else {
            GithubDependency githubDependency = (GithubDependency) dependency;
            return Objects.equals(githubDependency.owner, this.owner)
                    && Objects.equals(githubDependency.name, this.name)
                    && Objects.equals(githubDependency.ref, this.ref);
        }
    }

    @Override
    public Dependency copy() {
        return new GithubDependency(this.owner + "/" + this.name, this.ref, this.project);
    }

    @Override
    public String getGroup() {
        return this.owner;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return this.ref;
    }

    @Override
    public void because(String reason) {
        this.reason = reason;
    }

    @Override
    public String getReason() {
        return this.reason;
    }
}
