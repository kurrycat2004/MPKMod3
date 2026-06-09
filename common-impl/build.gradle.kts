plugins {
    id("jar-defaults-conventions")
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)

    implementation(projects.commonImplDeps)

    implementation(projects.commonApi)

    compileOnly(libs.jtoml)
    compileOnly(libs.jtoml.serializer.reflect)
}