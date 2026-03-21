package buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import java.util.*

@CacheableTask
abstract class MergeServiceFilesTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceRoots: ConfigurableFileCollection

    @get:Classpath
    abstract val sourceJars: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        outputDir.convention(
            project.layout.buildDirectory.dir("generated/mergedServices/${name}")
        )
    }

    @TaskAction
    fun merge() {
        val root = outputDir.get().asFile
        val servicesRoot = root.resolve("META-INF/services")

        root.deleteRecursively()
        servicesRoot.mkdirs()

        val merged = sortedMapOf<String, SortedSet<String>>()

        fun addServiceFile(serviceName: String, linesText: Sequence<String>) {
            val lines = merged.getOrPut(serviceName) { sortedSetOf() }
            linesText
                .map { it.substringBefore('#').trim() }
                .filter { it.isNotEmpty() }
                .forEach(lines::add)
        }

        sourceRoots.files
            .filter { it.exists() }
            .sortedBy { it.absolutePath }
            .forEach { sourceRoot ->
                val dir = sourceRoot.resolve("META-INF/services")
                if (!dir.isDirectory) return@forEach

                dir.listFiles()
                    ?.filter { it.isFile }
                    ?.sortedBy { it.name }
                    ?.forEach { serviceFile ->
                        addServiceFile(serviceFile.name, serviceFile.readLines(Charsets.UTF_8).asSequence())
                    }
            }

        sourceJars.files
            .filter { it.isFile && it.extension == "jar" }
            .sortedBy { it.absolutePath }
            .forEach { jarFile ->
                java.util.jar.JarFile(jarFile).use { jar ->
                    jar.entries().asSequence()
                        .filter { !it.isDirectory && it.name.startsWith("META-INF/services/") }
                        .sortedBy { it.name }
                        .forEach { entry ->
                            val serviceName = entry.name.removePrefix("META-INF/services/")
                            jar.getInputStream(entry).bufferedReader(Charsets.UTF_8).use { reader ->
                                addServiceFile(serviceName, reader.lineSequence())
                            }
                        }
                }
            }

        merged.forEach { (serviceName, impls) ->
            servicesRoot.resolve(serviceName)
                .writeText(impls.joinToString(separator = "\n", postfix = "\n"), Charsets.UTF_8)
        }
    }
}