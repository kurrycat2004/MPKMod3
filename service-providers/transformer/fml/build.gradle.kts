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
    implementation(projects.commonApi)

    implementation(projects.serviceProviders.api)
    compileOnly(libs.bundles.asm4)
}