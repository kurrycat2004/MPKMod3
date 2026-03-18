plugins {
    id("jar-defaults-conventions")
}

dependencies {
    compileOnly(libs.joml)
    compileOnlyApi(libs.jetbrains.annotations)
}