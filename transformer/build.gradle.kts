import buildlogic.MergeMetaTask
import buildlogic.annotationProcessor
import buildlogic.compileOnly
import buildlogic.excludeMeta
import buildlogic.mergeMeta

plugins {
    id("jar-defaults-conventions")
}

val glsc by sourceSets.creating

val variants = listOf(glsc)

dependencies {
    variants.forEach {
        it.compileOnly(
            this,
            projects.commonApi,
            libs.bundles.asm4,
            libs.auto.service.annotations
        )
        it.annotationProcessor(this, libs.auto.service)
    }
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