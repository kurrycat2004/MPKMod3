import buildlogic.MergeServiceFilesTask
import buildlogic.jars
import buildlogic.tasks

plugins {
    id("jar-defaults-conventions")
}

val components: List<Project> = listOf(
    projects.injectTags,
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

dependencies {
    embedDep(libs.jvmdowngrader) {
        exclude(group = "org.ow2.asm", module = "asm")
        exclude(group = "org.ow2.asm", module = "asm-tree")
        exclude(group = "org.ow2.asm", module = "asm-commons")
        exclude(group = "org.ow2.asm", module = "asm-util")
    }
    embedDep(libs.jtoml)
}

val mergeServiceFiles by tasks.registering(MergeServiceFilesTask::class) {
    tasks<Jar>(components, "jar").let {
        dependsOn(it)
        sourceJars.from(jars(it))
    }
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN

    tasks<Jar>(components, "jar").let {
        dependsOn(it)
        from(jars(it).map(::zipTree)) {
            exclude("META-INF/services/**")
        }
    }

    tasks<Jar>(modules, "jar").let {
        dependsOn(it)
        from(jars(it)) {
            into("mpkmodules")
        }
    }

    from(embedDep.resolve().map(::zipTree))

    from(mergeServiceFiles)

    from(rootProject.layout.projectDirectory.file("LICENSE"))
}

tasks.named<Jar>("sourcesJar") {
    tasks<Jar>(components, "sourcesJar").let {
        dependsOn(it)
        from(jars(it).map(::zipTree)) {
            exclude("META-INF/services/**")
        }
    }

    from(mergeServiceFiles)

    from(rootProject.layout.projectDirectory.file("LICENSE"))
}