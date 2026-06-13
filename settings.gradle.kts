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
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
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
include("common-api-deps")
include("common-impl")
include("common-impl-deps")
include("common-impl-deps:jvmdg")
include("inject-mod-metadata")

include("service-providers:api")
include("service-providers:lifecycle:fabric")
include("service-providers:lifecycle:forgeArchaic")
include("service-providers:lifecycle:forgeCommon")
include("service-providers:lifecycle:forgeLex")
include("service-providers:lifecycle:forgeVintage")
include("service-providers:log")
include("service-providers:lwjgl")
include("service-providers:transformer:fml")
include("service-providers:transformer:mixin")
include("service-providers:transformer:modlauncher")

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
