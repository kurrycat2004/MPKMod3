import buildlogic.MergeServiceFilesTask
import buildlogic.annotationProcessor
import buildlogic.compileOnly

plugins {
    id("jar-defaults-conventions")
}

val shared by sourceSets.creating
val opengl by sourceSets.creating { compileClasspath += shared.output }
val lwjgl2 by sourceSets.creating { compileClasspath += shared.output }
val lwjgl3 by sourceSets.creating { compileClasspath += shared.output }

val variants = listOf(shared, opengl, lwjgl2, lwjgl3)

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
