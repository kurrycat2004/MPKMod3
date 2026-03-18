plugins {
    id("jar-defaults-conventions")
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)

    compileOnly(libs.tomlj)
    compileOnly(libs.jvmdowngrader)

    compileOnly(projects.injectTags)
    compileOnly(projects.commonApi)
}