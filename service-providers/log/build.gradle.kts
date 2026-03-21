import buildlogic.MergeServiceFilesTask
import buildlogic.annotationProcessor
import buildlogic.compileOnly

plugins {
    id("jar-defaults-conventions")
}

val shared by sourceSets.creating
val log4j by sourceSets.creating { compileClasspath += shared.output }
val slf4j by sourceSets.creating { compileClasspath += shared.output }

val variants = listOf(shared, log4j, slf4j)

dependencies {
    variants.forEach {
        it.compileOnly(this, projects.commonApi)
        it.compileOnly(this, libs.auto.service.annotations)
        it.annotationProcessor(this, libs.auto.service)
    }
    log4j.compileOnly(this, libs.log4j.api)
    slf4j.compileOnly(this, libs.slf4j.api)
}

val mergeServiceFiles by tasks.registering(MergeServiceFilesTask::class) {
    sourceRoots.from(variants.map { it.output })
}

tasks.jar {
    from(variants.map { it.output }) {
        exclude("META-INF/services/**")
    }
    from(mergeServiceFiles)
}

tasks.named<Jar>("sourcesJar") {
    from(variants.map { it.allSource }) {
        exclude("META-INF/services/**")
    }
    from(mergeServiceFiles)
}