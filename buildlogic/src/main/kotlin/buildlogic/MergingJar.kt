package buildlogic

import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import javax.inject.Inject

abstract class MergingJar @Inject constructor(private val archives: ArchiveOperations) : Jar() {
    fun mergeJar(vararg jarTasks: TaskProvider<out Jar>) {
        jarTasks.forEach { jarTask ->
            dependsOn(jarTask)

            val archiveFile = jarTask.flatMap { it.archiveFile }

            from(archiveFile.map { file ->
                archives.zipTree(file.asFile)
            })
        }
    }

    fun mergeJars(configuration: Provider<Configuration>) {
        dependsOn(configuration)

        from(configuration.map {
            it.filter { file -> file.extension == "jar" }
                .map { file -> archives.zipTree(file) }
        })
    }
}