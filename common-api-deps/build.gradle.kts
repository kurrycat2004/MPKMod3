import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("jar-defaults-conventions")
}

val shadePrefix = "io.github.kurrycat.mpkmod.shadedlibs"

val relocate = configurations.create("relocate")

dependencies {
    relocate(libs.asm.tree)
    relocate(libs.joml) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
}

tasks.jar { enabled = false }
tasks.sourcesJar { enabled = false }

val apiJar = tasks.register<ShadowJar>("implementationJar") {
    description = "Bundles and relocates all required api deps"
    archiveAppendix.set("api-deps")

    configurations = listOf(relocate)
    exclude("module-info.class")
    exclude("META-INF/*.kotlin_module")

    relocate("org.objectweb.asm", "$shadePrefix.asm")
    relocate("org.joml", "$shadePrefix.joml")
}

tasks.assemble { dependsOn(apiJar) }

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
    it { outgoing.artifact(apiJar) }
}