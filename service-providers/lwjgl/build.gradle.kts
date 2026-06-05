import buildlogic.MergeMetaTask
import buildlogic.annotationProcessor
import buildlogic.compileOnly
import buildlogic.excludeMeta
import buildlogic.mergeMeta

plugins {
    id("jar-defaults-conventions")
}

val shared by sourceSets.creating
val opengl by sourceSets.creating { compileClasspath += shared.output }
val lwjgl2 by sourceSets.creating { compileClasspath += shared.output }
val lwjgl3 by sourceSets.creating { compileClasspath += shared.output }

val variants = listOf(shared, opengl, lwjgl2, lwjgl3)

repositories {
    mavenCentral()
    maven("https://maven.legacyfabric.net/")
}

dependencies {
    variants.forEach {
        it.compileOnly(this, projects.commonApi)
        it.compileOnly(this, libs.auto.service.annotations)
        it.annotationProcessor(this, libs.auto.service)
    }

    lwjgl2.compileOnly(this, libs.lwjgl2)
    lwjgl3.compileOnly(this, libs.lwjgl3)
    opengl.compileOnly(this, libs.joml)
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
