package buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*

@CacheableTask
abstract class GenerateModMetadata : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val metadataFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val relativeOutputPath: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val content: Property<String>

    @get:Internal
    val metadata: Provider<ModMetadata>
        get() = metadataFile.map { ModMetadata.read(it.asFile.toPath()) }

    init {
        metadataFile.convention(project.rootProject.layout.projectDirectory.file("mod-metadata.toml"))
        outputDir.convention(project.layout.buildDirectory.dir("generated/resources"))
        outputFile.convention(outputDir.file(relativeOutputPath))
    }

    @TaskAction
    fun generate() {
        val out = outputFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(content.get())
    }
}
