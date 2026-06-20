plugins {
    id("jar-defaults-conventions")
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)

    implementation(projects.commonApi)
    implementation(projects.serviceProviders.util)
}