import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("jar-defaults-conventions")
}

val shadePrefix = "io.github.kurrycat.mpkmod.shadedlibs"

val relocate = configurations.create("relocate")

repositories {
    mavenCentral()
    maven("https://maven.wagyourtail.xyz/snapshots/")
}

dependencies {
    relocate(libs.jtoml)
    relocate(libs.jtoml.serializer.reflect)
}

tasks.jar { enabled = false }
tasks.sourcesJar { enabled = false }

val implementationJar = tasks.register<ShadowJar>("implementationJar") {
    description = "Bundles and relocates all required implementation deps"
    archiveAppendix.set("impl-deps")

    configurations = listOf(relocate)
    exclude("module-info.class")

    relocate("io.github.wasabithumb.jtoml", "$shadePrefix.jtoml")
    relocate("io.github.wasabithumb.recsup", "$shadePrefix.recsup")

    mergeServiceFiles()
    failOnDuplicateEntries = true
}

tasks.assemble { dependsOn(implementationJar) }

listOf(
    configurations.apiElements,
    configurations.mainSourceElements,
    configurations.runtimeElements,
    configurations.sourcesElements,
    configurations.testResultsElementsForTest,
).forEach {
    it {
        outgoing.artifacts.clear()
        outgoing.variants.clear()
    }
}

listOf(configurations.apiElements, configurations.runtimeElements).forEach {
    it { outgoing.artifact(implementationJar) }
}