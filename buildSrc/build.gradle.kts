plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.shadow.gradle.plugin)
    implementation(libs.bundles.asm)
    implementation(libs.javapoet)
    implementation(libs.commons.compress)
}