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
+     }
  }
```

`build.gradle`:

```diff
  plugins {
      id 'fabric-loom' version '0.10-SNAPSHOT'
+     id 'io.github.enbrain.loom-layered-yarn' version '0.3.0'
      id 'maven-publish'
  }
```

## Examples

### Use `1.18.1-pre1+build.4` whenever possible, fallback to `1.17.1+build.65`

```groovy
mappings loom.layered() {
    addLayer(loomLayeredYarn.yarn("net.fabricmc:yarn:1.17.1+build.65:v2"))
    addLayer(loomLayeredYarn.yarn("net.fabricmc:yarn:1.18.1-pre1+build.4:v2"))
}
```

### Use [Yarn PR #2903](https://github.com/FabricMC/yarn/pull/2903) and [Yarn PR #2895](https://github.com/FabricMC/yarn/pull/2895) on top of `1.18.1-pre1+build.4`

```groovy
mappings loom.layered() {
    addLayer(loomLayeredYarn.yarn("net.fabricmc:yarn:1.18.1-pre1+build.4:v2"))
    addLayer(loomLayeredYarn.yarn(loomLayeredYarn.github("apple502j/yarn", "1.18.1-pre1-collision")) {
        base = "net.fabricmc:yarn:1.18.1-pre1+build.4:v2"
    })
    addLayer(loomLayeredYarn.yarn(loomLayeredYarn.github("haykam821/yarn", "gameoptions-key-suffix")) {
        base = "net.fabricmc:yarn:1.18+build.1:v2"
    })
}
```

### Use local Yarn repository

```groovy
mappings loom.layered() {
    addLayer(loomLayeredYarn.yarn(file("../yarn")))
}
```
