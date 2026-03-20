import buildlogic.annotationProcessor
import buildlogic.compileOnly

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
            libs.bundles.comp.core.asm4,
            libs.auto.service.annotations
        )
        it.annotationProcessor(this, libs.auto.service)
    }
}

tasks.jar {
    from(variants.map { it.output })
}

tasks.named<Jar>("sourcesJar") {
    from(variants.map { it.allSource })
}