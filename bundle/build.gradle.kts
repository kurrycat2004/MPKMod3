import buildlogic.MergeMetaTask
import buildlogic.excludeMeta
import buildlogic.jars
import buildlogic.mergeMeta
import buildlogic.registerDowngradedJvmVariant
import buildlogic.tasks

plugins {
    id("jar-defaults-conventions")
}

val components: List<Project> = listOf(
    projects.injectModMetadata,
    projects.commonApi,
    projects.commonImpl,
    projects.serviceProviders.log,
    projects.serviceProviders.lwjgl,
    //projects.serviceProviders.transformer,
    projects.serviceProviders.entrypoint.transformer,
    projects.serviceProviders.entrypoint.loader,
).map { evaluationDependsOn(it.path); project(it.path) }

val modules = listOf(
    projects.modules.main,
).map { evaluationDependsOn(it.path); project(it.path) }

val embedDep by configurations.creating

repositories {
    mavenCentral()
    maven("https://maven.wagyourtail.xyz/snapshots/")
}

dependencies {
    embedDep(libs.jvmdowngrader) {
        exclude(group = "org.ow2.asm", module = "asm")
        exclude(group = "org.ow2.asm", module = "asm-tree")
        exclude(group = "org.ow2.asm", module = "asm-commons")
        exclude(group = "org.ow2.asm", module = "asm-util")
    }
    embedDep(libs.jtoml)
}

val mergeMetaTask by tasks.registering(MergeMetaTask::class) {
    tasks<Jar>(components, "jar").let {
        dependsOn(it)
        sourceJars.from(jars(it))
    }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.WARN

    tasks<Jar>(components, "jar").let {
        dependsOn(it)
        from(jars(it).map(::zipTree)) { excludeMeta() }
    }

    tasks<Jar>(modules, "jar").let {
        dependsOn(it)
        from(jars(it)) {
            into("mpkmodules")
        }
    }

    from(embedDep.resolve().map(::zipTree))

    mergeMeta(mergeMetaTask)

    from(rootProject.layout.projectDirectory.file("LICENSE"))
}

registerDowngradedJvmVariant(
    JavaVersion.VERSION_1_8,
    sourceSets.main.map { it.compileClasspath }
)

tasks.sourcesJar {
    tasks<Jar>(components, "sourcesJar").let {
        dependsOn(it)
        from(jars(it).map(::zipTree)) { excludeMeta() }
    }

    mergeMeta(mergeMetaTask)

    from(rootProject.layout.projectDirectory.file("LICENSE"))
}