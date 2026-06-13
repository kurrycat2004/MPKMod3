plugins {
    id("jar-defaults-conventions")
}

val stubs = sourceSets.create("stubs")
sourceSets.main {
    compileClasspath += stubs.output
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)

    implementation(projects.commonApi)

    implementation(projects.serviceProviders.api)
    implementation(projects.serviceProviders.lifecycle.forgeCommon)
}