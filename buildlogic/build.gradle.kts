plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.bundles.asm)
    implementation(libs.javapoet)
    implementation(libs.commons.compress)
    implementation(libs.jtoml)
    implementation(libs.jtoml.serializer.reflect)
}