# loom-layered-yarn
Layered mappings for Yarn.

## Usage Example
`settings.gradle`:

```diff
  pluginManagement {
      repositories {
          maven {
              name = 'Fabric'
              url = 'https://maven.fabricmc.net/'
          }
+         maven {
+             name = 'JitPack'
+             url = 'https://jitpack.io/'
+         }
          mavenCentral()
          gradlePluginPortal()
      }
+     resolutionStrategy {
+         eachPlugin {
+             if (requested.id.id == 'io.github.enbrain.loom-layered-yarn') {
+                 useModule("com.github.enbrain:loom-layered-yarn:${requested.version}")
+             }
+         }
+   }
  }
```

`build.gradle`:

```diff
  plugins {
      id 'fabric-loom' version '0.10-SNAPSHOT'
+     id 'io.github.enbrain.loom-layered-yarn' version '0.1.0'
      id 'maven-publish'
  }

  dependencies {
      minecraft "com.mojang:minecraft:${project.minecraft_version}"
-     mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
+     mappings loom.layered() {
+         // Uses `snapshot_yarn_mappings` whenever possible, fallbacks to `yarn_mapping`
+         addLayer(loomLayeredYarn.yarn("net.fabricmc:yarn:${project.yarn_mappings}:v2"))
+         addLayer(loomLayeredYarn.yarn("net.fabricmc:yarn:${project.snapshot_yarn_mappings}:v2"))
+     }
      modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
  }
```

`gradle.properties`:

```diff
  # Fabric Properties
      # check these on https://fabricmc.net/versions.html
      minecraft_version=1.17.1
      yarn_mappings=1.17.1+build.65
+     snapshot_yarn_mappings=1.18.1-pre1+build.4
      loader_version=0.12.8
```
