plugins {
    id("jar-defaults-conventions")
}

repositories {
    mavenCentral()
    maven("https://files.minecraftforge.net/maven/")
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)
    compileOnly(projects.commonApi)

    compileOnly(libs.modlauncher)
    compileOnly(libs.bundles.asm4)
}
