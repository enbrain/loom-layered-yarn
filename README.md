# loom-layered-yarn
Layered mappings for Yarn.

## How to setup for use

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
+     id 'io.github.enbrain.loom-layered-yarn' version '0.2.0'
      id 'maven-publish'
  }
```

## Examples

### Apply `1.18.1-pre1+build.4` whenever possible, fallback to `1.17.1+build.65`

`build.gradle`:

```groovy
mappings loom.layered() {
    addLayer(loomLayeredYarn.yarn("net.fabricmc:yarn:1.17.1+build.65:v2"))
    addLayer(loomLayeredYarn.yarn("net.fabricmc:yarn:1.18.1-pre1+build.4:v2"))
}
```

### Apply [Yarn PR #2903](https://github.com/FabricMC/yarn/pull/2903) and [Yarn PR #2895](https://github.com/FabricMC/yarn/pull/2895) on top of `1.18.1-pre1+build.4`

```groovy

mappings loom.layered() {
    addLayer(loomLayeredYarn.yarn("net.fabricmc:yarn:1.18.1-pre1+build.4:v2"))
    // repo, branch, base
    addLayer(loomLayeredYarn.githubDiff("apple502j/yarn", "1.18.1-pre1-collision", "net.fabricmc:yarn:1.18.1-pre1+build.4:v2"))
    addLayer(loomLayeredYarn.githubDiff("haykam821/yarn", "gameoptions-key-suffix", "net.fabricmc:yarn:1.18+build.1:v2"))
}
```