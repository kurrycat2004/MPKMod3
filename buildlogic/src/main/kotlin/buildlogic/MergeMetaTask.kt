package buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.util.*
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest

@CacheableTask
abstract class MergeMetaTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceRoots: ConfigurableFileCollection

    @get:Classpath
    abstract val sourceJars: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        outputDir.convention(project.layout.buildDirectory.dir("generated/mergedMeta/${name}"))
    }

    @TaskAction
    fun merge() {
        val root = outputDir.get().asFile
        val servicesRoot = root.resolve("META-INF/services")
        val manifestOut = root.resolve("META-INF/MANIFEST.MF")

        root.deleteRecursively()
        servicesRoot.mkdirs()
        manifestOut.parentFile.mkdirs()

        mergeServices(servicesRoot)
        mergeManifests(manifestOut)
    }

    private fun mergeServices(servicesRoot: File) {
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
                JarFile(jarFile).use { jar ->
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

    private fun mergeManifests(manifestOut: File) {
        val merged = Manifest()
        merged.mainAttributes.putValue("Manifest-Version", "1.0")

        fun mergeManifest(manifest: Manifest, source: String) {
            manifest.mainAttributes.entries
                .sortedBy { it.key.toString() }
                .forEach { (keyAny, valueAny) ->
                    val key = keyAny.toString()
                    val value = valueAny.toString()

                    if (key.equals("Manifest-Version", ignoreCase = true)) {
                        return@forEach
                    }

                    val old = merged.mainAttributes.getValue(key)
                    if (old != null && old != value) {
                        logger.info("Manifest attribute '$key' from $source overrides '$old' with '$value'")
                    }

                    merged.mainAttributes.putValue(key, value)
                }

            manifest.entries.toSortedMap().forEach { (sectionName, attrs) ->
                val target = merged.entries.getOrPut(sectionName) { Attributes() }

                attrs.entries
                    .sortedBy { it.key.toString() }
                    .forEach { (key, value) -> target[key] = value }
            }
        }

        sourceRoots.files
            .filter { it.exists() }
            .sortedBy { it.absolutePath }
            .forEach { sourceRoot ->
                val mf = sourceRoot.resolve("META-INF/MANIFEST.MF")
                if (mf.isFile) {
                    mf.inputStream().use {
                        mergeManifest(Manifest(it), mf.path)
                    }
                }
            }

        sourceJars.files
            .filter { it.isFile && it.extension.equals("jar", ignoreCase = true) }
            .sortedBy { it.absolutePath }
            .forEach { jarFile ->
                JarFile(jarFile).use { jar ->
                    jar.manifest?.let { mergeManifest(it, jarFile.name) }
                }
            }

        manifestOut.outputStream().use { out ->
            merged.write(out)
        }
    }
}

fun Jar.mergeMeta(mergeMetaTask: TaskProvider<MergeMetaTask>) {
    from(mergeMetaTask) {
        include("META-INF/services/**")
    }
    manifest {
        from(mergeMetaTask.map { it.outputDir.file("META-INF/MANIFEST.MF") }.get())
    }
}

fun CopySpec.excludeMeta() {
    exclude("META-INF/services/**")
    exclude("META-INF/MANIFEST.MF")
}