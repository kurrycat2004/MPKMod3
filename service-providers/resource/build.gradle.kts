import buildlogic.MergeMetaTask
import buildlogic.annotationProcessor
import buildlogic.compileOnly
import buildlogic.excludeMeta
import buildlogic.implementation
import buildlogic.mergeMeta

plugins {
    id("jar-defaults-conventions")
}

val seargeStubs = sourceSets.create("seargeStubs")
val searge = sourceSets.create("searge") {
    compileClasspath += seargeStubs.output
}

val mcpStubs = sourceSets.create("mcpStubs")
val mcp = sourceSets.create("mcp") {
    compileClasspath += mcpStubs.output
}

val variants = listOf(searge, mcp)

dependencies {
    variants.forEach {
        it.compileOnly(this, libs.auto.service.annotations)
        it.annotationProcessor(this, libs.auto.service)

        it.implementation(this, projects.commonApi)
        it.implementation(this, projects.serviceProviders.util)
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