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
    compileOnly(projects.commonApi)

    implementation(projects.serviceProviders.lifecycle.forgeCommon)
}