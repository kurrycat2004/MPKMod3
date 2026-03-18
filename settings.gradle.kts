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

include("common-dep-impl:core")
include("common-dep-impl:log")
include("common-dep-impl:lwjgl")

include("common-impl")

include("modules:main")