package buildlogic

import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import xyz.wagyourtail.jvmdg.ClassDowngrader
import xyz.wagyourtail.jvmdg.compile.PathDowngrader
import xyz.wagyourtail.jvmdg.gradle.flags.FlagsConvention
import xyz.wagyourtail.jvmdg.gradle.flags.ShadeFlags
import xyz.wagyourtail.jvmdg.gradle.flags.toFlags
import xyz.wagyourtail.jvmdg.util.Utils
import java.nio.file.FileSystem

abstract class DowngradeJars : DefaultTask(), ShadeFlags, FlagsConvention {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val inputJars: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val classpath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        group = "JVMDowngrader"
        outputDirectory.convention(project.layout.buildDirectory.dir("jvmdg/$name"))

        convention(
            project.gradle.sharedServices.registrations
                .getByName("${project.path}:jvmdgDefaultFlags")
                .parameters as ShadeFlags
        )
    }

    override fun shadePath(action: Closure<String>) {
        shadePath.set { action.call(it) }
    }

    @TaskAction
    fun doDowngrade() {
        val outDir = outputDirectory.get().asFile
        outDir.deleteRecursively()
        outDir.mkdirs()

        val inputFileSystems = mutableSetOf<FileSystem>()
        val outputFileSystems = mutableSetOf<FileSystem>()

        try {
            inputJars.files
                .filter { it.exists() }
                .forEach { inputJar ->
                    val outputJar = outDir.resolve(inputJar.name)

                    inputFileSystems += Utils.openZipFileSystem(inputJar.toPath(), false)
                    outputFileSystems += Utils.openZipFileSystem(outputJar.toPath(), true)
                }

            ClassDowngrader.downgradeTo(this.toFlags()).use { downgrader ->
                PathDowngrader.downgradePaths(
                    downgrader,
                    inputFileSystems.map { it.getPath("/") },
                    outputFileSystems.map { it.getPath("/") },
                    classpath.map { it.toURI().toURL() }.toSet()
                )
            }
        } finally {
            inputFileSystems.forEach { it.close() }
            outputFileSystems.forEach { it.close() }
        }
    }
}
