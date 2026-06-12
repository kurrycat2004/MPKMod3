plugins {
    id("jar-defaults-conventions")
}

repositories {
    mavenCentral()
    maven("https://files.minecraftforge.net/maven/")
}

dependencies {
    compileOnly(projects.commonApi)

    compileOnly(libs.mixin)
    compileOnly(libs.bundles.asm5)
}