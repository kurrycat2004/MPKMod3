package buildlogic

import org.gradle.api.Action
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.bundling.Jar
import javax.inject.Inject

abstract class MergingJar : Jar() {
    @get:Inject
    abstract val archives: ArchiveOperations

    @get:Classpath
    abstract val inputJars: ConfigurableFileCollection

    fun mergeJars(action: Action<in CopySpec> = Action {}) {
        from(inputJars.elements.map { elements ->
            elements
                .map { it.asFile }
                .filter { it.extension == "jar" }
                .map { archives.zipTree(it) }
        }, action)
    }
}