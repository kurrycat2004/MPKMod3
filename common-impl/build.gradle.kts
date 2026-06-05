plugins {
    id("jar-defaults-conventions")
}

repositories {
    mavenCentral()
    maven("https://maven.wagyourtail.xyz/snapshots/")
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)

    compileOnly(libs.jtoml)
    compileOnly(libs.jtoml.serializer.reflect)
    compileOnly(libs.jvmdowngrader)

    compileOnly(projects.commonApi)
}