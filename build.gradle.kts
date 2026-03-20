import buildlogic.MergeServiceFilesTask
import org.gradle.api.internal.catalog.DelegatingProjectDependency

plugins {
    id("jar-defaults-conventions")
}

val components: List<DelegatingProjectDependency> = listOf(
    projects.injectTags,
    projects.commonApi,
    projects.commonImpl,
    projects.serviceProviders.log,
    projects.serviceProviders.lwjgl,
    //projects.serviceProviders.transformer,
    projects.serviceProviders.entrypoint.transformer,
    projects.modules.main
)

val componentProjects = components.map { it.path }.map(::project)

val mergeServiceFiles by tasks.registering(MergeServiceFilesTask::class) {
    val componentJarTasks = componentProjects.map { it.tasks.named<Jar>("jar") }
    dependsOn(componentJarTasks)

    sourceJars.from(componentJarTasks.map { it.flatMap(Jar::getArchiveFile) })
}

tasks.named<Jar>("jar") {
    val componentJarTasks = componentProjects.map { it.tasks.named<Jar>("jar") }
    dependsOn(componentJarTasks)

    componentJarTasks
        .map { it.flatMap(Jar::getArchiveFile) }
        .map { zipTree(it) }
        .let {
            from(it) {
                exclude("META-INF/services/**")
            }
        }

    from(mergeServiceFiles)
}

tasks.named<Jar>("sourcesJar") {
    val componentSourceJarTasks = componentProjects.map { it.tasks.named<Jar>("sourcesJar") }
    dependsOn(componentSourceJarTasks)

    componentSourceJarTasks
        .map { it.flatMap(Jar::getArchiveFile) }
        .map { zipTree(it) }
        .let {
            from(it) {
                exclude("META-INF/services/**")
            }
        }

    from(mergeServiceFiles)
}