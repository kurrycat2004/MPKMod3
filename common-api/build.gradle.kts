plugins {
    id("jar-defaults-conventions")
}

dependencies {
    compileOnly(libs.comp.core.asm4.tree)
    compileOnly(libs.joml)
    compileOnlyApi(libs.jetbrains.annotations)
}