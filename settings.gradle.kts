enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "MPKMod"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://files.minecraftforge.net/maven")
        //maven("https://maven.fabricmc.net/")
        maven("https://maven.legacyfabric.net")
        //maven("https://maven.minecraftforge.net/")
        //maven("https://maven.neoforged.net/releases/")
        //maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
        //maven("https://repo.spongepowered.org/maven/")
    }
}

include("inject-tags")
include("common-api")

include("service-providers:log")
include("service-providers:lwjgl")
include("service-providers:transformer")
include("service-providers:entrypoint:transformer")

include("common-impl")

include("modules:main")