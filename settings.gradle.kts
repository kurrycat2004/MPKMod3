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
        //maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
        //maven("https://nexus.gtnewhorizons.com/repository/public/")
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
    }
}

includeBuild("buildlogic")

include("bundle")
include("common-api")
include("common-impl")
include("inject-mod-metadata")
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

include("runs:forge")
runConfigurations.forge.forEach {
    file("runs/forge/${it.projectName()}").mkdirs()
    include("runs:forge:${it.projectName()}")
}
