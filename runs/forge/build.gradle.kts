import buildlogic.RunConfiguration
import buildlogic.projectName
import com.gtnewhorizons.retrofuturagradle.MinecraftExtension
import com.gtnewhorizons.retrofuturagradle.minecraft.RunMinecraftTask

plugins {
    alias(libs.plugins.retrofuturagradle) apply false
}

val rfgPluginId = libs.plugins.retrofuturagradle.get().pluginId;
val bundleProject = projects.bundle
val lwjgl2 = libs.versions.lwjgl2.get()

val runConfigurationFile = file("../run-configuration.toml")
val runConfigurations: RunConfiguration = RunConfiguration.read(runConfigurationFile.toPath())

runConfigurations.forge.forEach { forge ->
    project(":runs:forge:${forge.projectName()}") {
        pluginManager.apply(rfgPluginId)

        repositories {
            maven("https://maven.legacyfabric.net/")
        }

        extensions.configure<MinecraftExtension> {
            mcVersion = forge.mcVersion
            //lwjgl2Version = lwjgl2
        }

        tasks.named<RunMinecraftTask>("runClient") {
            workingDir = file("../run")
            extraArgs = listOf("-Dmpkmod.module.enableModuleLoadStacktrace=true")
        }

        dependencies {
            //add("runtimeOnly", bundleProject)
        }

        /*
        dependencies {
            add("minecraft", "com.mojang:minecraft:${forge.version}")
            add("mappings", "de.oceanlabs.mcp:mcp_stable:22-1.8.9")
            add("forge", "net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
            add("runtimeOnly", bundleProject)
        }
     */
    }
}
