# Layered Yarn
A gradle plugin that allows Yarn to be used as a mapping layer in Loom.

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
+     id 'io.github.enbrain.loom-layered-yarn' version '0.6.0'
      id 'maven-publish'
  }
```

## Examples

### Use `1.18.1-rc1+build.1` whenever possible, fallback to `1.17.1+build.65`

```groovy
mappings loom.layered() {
    addLayer(layeredYarn.yarn("net.fabricmc:yarn:1.17.1+build.65:v2"))
    addLayer(layeredYarn.yarn("net.fabricmc:yarn:1.18.1-rc1+build.1:v2"))
}
```

### Use [Yarn PR #2903](https://github.com/FabricMC/yarn/pull/2903) and [Yarn PR #2895](https://github.com/FabricMC/yarn/pull/2895) on top of `1.18.1-rc1+build.1`

```groovy
mappings loom.layered() {
    addLayer(layeredYarn.yarn("net.fabricmc:yarn:1.18.1-rc1+build.1:v2"))
    addLayer(layeredYarn.yarn(layeredYarn.github("apple502j/yarn", "1.18.1-pre1-collision")) {
        base = "net.fabricmc:yarn:1.18.1-pre1+build.4:v2"
    })
    addLayer(layeredYarn.yarn(layeredYarn.github("haykam821/yarn", "gameoptions-key-suffix")) {
        base = "net.fabricmc:yarn:1.18+build.1:v2"
    })
}
```

### Use local Yarn repository

```groovy
mappings loom.layered() {
    addLayer(layeredYarn.yarn(file("../yarn")))
}
```

### Show changes of [Yarn PR #2895](https://github.com/FabricMC/yarn/pull/2895) in Javadoc

```groovy
mappings loom.layered() {
    addLayer(layeredYarn.appendToJavadoc() {
        base = loom.layered() {
            addLayer(layeredYarn.yarn("net.fabricmc:yarn:1.18.1-rc1+build.1:v2"))
        }
        additional = loom.layered() {
            addLayer(layeredYarn.yarn(layeredYarn.github("haykam821/yarn", "gameoptions-key-suffix")) {
                base = "net.fabricmc:yarn:1.18+build.1:v2"
            })
        }
        prefix = "@keySuffixRefactor"
    })
}
```

Result:

```java
/**
 * A key binding for moving forward.
 * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_W the W key} by default.
 * 
 * @keySuffixRefactor forwardKey
 */
public final KeyBinding keyForward;
```

Note: Showing Javadoc changes is not supported.

### Use unpick

```groovy
dependencies {
    mappings layeredYarn.enableUnpick(loom.layered() {
        addLayer(layeredYarn.yarn("net.fabricmc:yarn:1.18.1+build.22:v2"));
        addLayer(layeredYarn.unpick("net.fabricmc:yarn:1.18.1+build.22:v2"))
    })
}

configurations.mappingsConstants {
    withDependencies { dependencies ->
        dependencies.removeIf { it.group == "loom" && it.name == "mappings" }
        dependencies.add(project.dependencies.create("net.fabricmc:yarn:1.18.1+build.22:constants"))
    }
}
```
