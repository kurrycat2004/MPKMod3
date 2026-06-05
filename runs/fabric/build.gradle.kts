import buildlogic.RunConfiguration
import buildlogic.projectName
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    alias(libs.plugins.fabric.loom.remap) apply false
}

val fabricLoomRemapPluginId = libs.plugins.fabric.loom.remap.get().pluginId
val bundleProject = projects.bundle
val fabricLoader = libs.fabric.loader

val runConfigurationFile = file("../run-configuration.toml")
val runConfigurations: RunConfiguration = RunConfiguration.read(runConfigurationFile.toPath())

runConfigurations.fabric.forEach { fabric ->
    project(":runs:fabric:${fabric.projectName()}") {
        pluginManager.apply(fabricLoomRemapPluginId)

        repositories {
            maven("https://maven.legacyfabric.net/")
        }

        dependencies {
            add("minecraft", "com.mojang:minecraft:${fabric.minecraft}")
            add("mappings", fabric.mappings)
            add("modImplementation", fabricLoader)
            add("runtimeOnly", bundleProject)
        }

        extensions.configure<LoomGradleExtensionAPI> {
            runs {
                named("client") {
                    runDir("../run/client")
                    vmArgs("-Dmpkmod.module.enableModuleLoadStacktrace=true")

                    if (fabric.minecraft.startsWith("1.7")) {
                        programArgs("--userProperties", "{}")
                    }
                }
            }
        }
    }
}