import buildlogic.annotationProcessor
import buildlogic.compileOnly

plugins {
    id("jar-defaults-conventions")
}

val fmlStubs by sourceSets.creating
val fml by sourceSets.creating { compileClasspath += fmlStubs.output }
val mixin by sourceSets.creating
val modlauncher by sourceSets.creating

val variants = listOf(fml, mixin, modlauncher)

dependencies {
    variants.forEach { it.compileOnly(this, projects.commonApi) }

    fml.compileOnly(this, libs.bundles.comp.core.asm4)

    mixin.compileOnly(this, libs.comp.core.mixin)
    mixin.compileOnly(this, libs.bundles.comp.core.asm5)

    modlauncher.compileOnly(this, libs.auto.service.annotations)
    modlauncher.annotationProcessor(this, libs.auto.service)
    modlauncher.compileOnly(this, libs.comp.core.modlauncher)
    modlauncher.compileOnly(this, libs.bundles.comp.core.asm4)
}

tasks.jar {
    from(variants.map { it.output })
}

tasks.named<Jar>("sourcesJar") {
    from(variants.map { it.allSource })
}

