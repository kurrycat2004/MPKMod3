import buildlogic.MergeMetaTask
import buildlogic.annotationProcessor
import buildlogic.compileOnly
import buildlogic.excludeMeta
import buildlogic.implementation
import buildlogic.mergeMeta

plugins {
    id("jar-defaults-conventions")
}

val shared by sourceSets.creating
val log4j by sourceSets.creating { compileClasspath += shared.output }
val slf4j by sourceSets.creating { compileClasspath += shared.output }

val variants = listOf(shared, log4j, slf4j)

dependencies {
    variants.forEach {
        it.compileOnly(this, libs.auto.service.annotations)
        it.annotationProcessor(this, libs.auto.service)

        it.implementation(this, projects.commonApi)
        it.implementation(this, projects.serviceProviders.util)
    }
    log4j.compileOnly(this, libs.log4j.api)
    slf4j.compileOnly(this, libs.slf4j.api)
}

val mergeMetaTask by tasks.registering(MergeMetaTask::class) {
    sourceRoots.from(variants.map { it.output })
}

tasks.jar {
    from(variants.map { it.output }) { excludeMeta() }
    mergeMeta(mergeMetaTask)
}

tasks.sourcesJar {
    from(variants.map { it.allSource }) { excludeMeta() }
    mergeMeta(mergeMetaTask)
}