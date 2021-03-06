# Layered Yarn
A Gradle plugin that provides utilities for [Fabric Loom](https://github.com/FabricMC/fabric-loom) layered mappings, notably GitHub dependency and local directory file spec.

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
      id 'fabric-loom' version '0.11-SNAPSHOT'
+     id 'io.github.enbrain.loom-layered-yarn' version '0.10.2'
      id 'maven-publish'
  }
```

## Examples

### Use `1.18.1+build.22` whenever possible, fallback to `22w03a+build.12`

```groovy
mappings loom.layered() {
    addLayer layeredYarn.yarn("net.fabricmc:yarn:22w03a+build.12:v2")
    addLayer layeredYarn.yarn("net.fabricmc:yarn:1.18.1+build.22:v2")
}
```

### Use unpick

```groovy
mappings layeredYarn.enableUnpick(loom.layered() {
    addLayer layeredYarn.yarn("net.fabricmc:yarn:22w03a+build.12:v2")
    addLayer layeredYarn.unpick("net.fabricmc:yarn:22w03a+build.12:v2")
})
```

This automatically adds the constants jar `yarn-22w03a+build.12-constants.jar` to the dependencies.

### Use local Yarn repository

```groovy
mappings loom.layered() {
    addLayer layeredYarn.yarn(file("../yarn"))
}
```

### Use [Yarn PR #2921](https://github.com/FabricMC/yarn/pull/2921) and [Yarn PR #2978](https://github.com/FabricMC/yarn/pull/2978) on top of `22w03a+build.12`

```groovy
mappings loom.layered() {
    addLayer layeredYarn.yarn("net.fabricmc:yarn:22w03a+build.12:v2")
    addLayer layeredYarn.pr(2921)
    addLayer layeredYarn.pr(2978)
}
```

### Show changes of [Yarn PR #2921](https://github.com/FabricMC/yarn/pull/2921) in Javadoc

```groovy
mappings loom.layered() {
    addLayer layeredYarn.appendToJavadoc() {
        base = loom.layered() {
             addLayer layeredYarn.yarn("net.fabricmc:yarn:22w03a+build.12:v2")
        }
        add "builderRefactor", loom.layered() {
            addLayer layeredYarn.pr(2921)
        }
    }
}
```

Result:

```java
/**
 * @builderRefactor advancementBuilder
 */
private final Advancement.Task builder = Advancement.Task.create();
```

Note: Showing Javadoc changes is not supported.
