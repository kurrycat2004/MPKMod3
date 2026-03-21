@file:Suppress("PropertyName")

plugins {
    alias(libs.plugins.fabric.loom.remap)
}

val v1_20_1 = sourceSets.create("1.20.1")

val variants = listOf(v1_20_1)

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.18.4")
    runtimeOnly(projects.bundle)
}

loom {

}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}