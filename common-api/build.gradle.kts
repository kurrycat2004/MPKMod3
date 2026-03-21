plugins {
    id("jar-defaults-conventions")
}

dependencies {
    compileOnly(libs.asm4.tree)
    compileOnly(libs.joml)
    compileOnlyApi(libs.jetbrains.annotations)
}