import buildlogic.annotationProcessor
import buildlogic.compileOnly

plugins {
    id("jar-defaults-conventions")
}

val shared by sourceSets.creating
val fmlStubs by sourceSets.creating
val fml by sourceSets.creating { compileClasspath += shared.output + fmlStubs.output }
val mixin by sourceSets.creating { compileClasspath += shared.output }
val modlauncher by sourceSets.creating { compileClasspath += shared.output }

val variants = listOf(shared, fml, mixin, modlauncher)

dependencies {
    variants.forEach { it.compileOnly(this, projects.injectTags, projects.commonApi) }

    shared.compileOnly(this, libs.auto.service.annotations)
    shared.annotationProcessor(this, libs.auto.service)
    shared.compileOnly(this, libs.bundles.comp.core.asm4)

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

