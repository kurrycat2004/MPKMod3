plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.wagyourtail.xyz/snapshots/")
}

dependencies {
    implementation(libs.bundles.asm)
    implementation(libs.javapoet)
    implementation(libs.commons.compress)
    implementation(libs.jtoml)
    implementation(libs.jtoml.serializer.reflect)
    implementation(libs.shadow)
    implementation(libs.jvmdowngrader.plugin)
}