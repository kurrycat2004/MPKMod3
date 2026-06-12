plugins {
    id("jar-defaults-conventions")
}

val stubs = sourceSets.create("stubs")
sourceSets.main {
    compileClasspath += stubs.output
}

repositories {
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    compileOnly(projects.commonApi)
    compileOnly(libs.bundles.asm4)
}