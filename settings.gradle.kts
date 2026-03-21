import buildlogic.RunConfiguration
import buildlogic.projectName

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "MPKMod"

pluginManagement {
    includeBuild("buildlogic")

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://files.minecraftforge.net/maven")
        maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
    }
}

plugins {
    id("buildlogic")
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://files.minecraftforge.net/maven")
        maven("https://maven.fabricmc.net")
        maven("https://maven.legacyfabric.net")
        //maven("https://maven.minecraftforge.net")
        //maven("https://maven.neoforged.net/releases")
        //maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
        //maven("https://repo.spongepowered.org/maven")
    }
}

includeBuild("buildlogic")

include("bundle")
include("common-api")
include("common-impl")
include("inject-tags")
include("service-providers:log")
include("service-providers:lwjgl")
include("service-providers:transformer")
include("service-providers:entrypoint:transformer")
include("service-providers:entrypoint:loader")

include("modules:main")


// run configurations
val runConfigurationFile = file("runs/run-configuration.toml")
val runConfigurations: RunConfiguration = RunConfiguration.read(runConfigurationFile.toPath())
include("runs:fabric")
runConfigurations.fabric.forEach {
    file("runs/fabric/${it.projectName()}").mkdirs()
    include("runs:fabric:${it.projectName()}")
}
