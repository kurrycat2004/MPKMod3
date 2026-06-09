import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("jar-defaults-conventions")
}

val shadePrefix = "io.github.kurrycat.mpkmod.shadedlibs"

val relocate = configurations.create("relocate")

val jvmdgJavaApi = configurations.create("jvmdgJavaApi") {
    isTransitive = false
}

repositories {
    mavenCentral()
    maven("https://maven.wagyourtail.xyz/snapshots/")
}

dependencies {
    relocate(libs.jvmdowngrader) {
        isTransitive = false
    }
    jvmdgJavaApi(libs.jvmdowngrader.java.api) {
        artifact { classifier = "downgraded-8" }
    }
}

tasks.jar { enabled = false }
tasks.sourcesJar { enabled = false }

val jvmdgJavaApiJar = tasks.register<ShadowJar>("jvmdgJavaApiJar") {
    group = "jvmdowngrader"
    description = "Creates the java-api jar for jvmdg with relocated asm."
    archiveBaseName = "java-api"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    failOnDuplicateEntries = true

    configurations = listOf(jvmdgJavaApi)
    exclude("module-info.class")
    relocate("org.objectweb.asm", "$shadePrefix.asm")
}

val implementationJar = tasks.register<ShadowJar>("implementationJar") {
    description = "Bundles and relocates all required implementation deps"
    archiveAppendix.set("impl-deps")

    configurations = listOf(relocate)
    exclude("module-info.class")

    relocate("org.objectweb.asm", "$shadePrefix.asm")

    from(jvmdgJavaApiJar.map { it.archiveFile }) {
        into("META-INF/lib")
        rename { "java-api.jar" }
    }
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