plugins {
    id("jar-defaults-conventions")
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)

    compileOnly(libs.jtoml)
    compileOnly(libs.jtoml.serializer.reflect)
    compileOnly(libs.jvmdowngrader)

    compileOnly(projects.injectTags)
    compileOnly(projects.commonApi)
}