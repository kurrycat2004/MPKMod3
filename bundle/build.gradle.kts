import buildlogic.DowngradeJars
import buildlogic.MergeMetaTask
import buildlogic.MergingJar
import buildlogic.ShadeJars
import buildlogic.excludeMeta
import buildlogic.mergeMeta
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.artifacts.configurations.ConfigurationsProvider
import org.gradle.internal.extensions.stdlib.capitalized
import xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar
import xyz.wagyourtail.jvmdg.gradle.task.ShadeJar

plugins {
    id("jar-defaults-conventions")
    id("xyz.wagyourtail.jvmdowngrader")
}

val jvmdgJavaApi = configurations.create("jvmdgJavaApi") {
    isTransitive = false
}
val moduleJar = configurations.create("moduleJar")

val embed = configurations.create("embed")

val subproject = configurations.create("subproject") {
    configurations.named(embed.name) { extendsFrom(this@create) }
}

val jvmdg = configurations.register("jvmdg")
val subprojectDep = configurations.create("subprojectDep")

dependencies {
    subproject(projects.commonApi) { isTransitive = false }
    subprojectDep(projects.commonApiDeps)
    subproject(projects.commonImpl) { isTransitive = false }
    subprojectDep(projects.commonImplDeps)
    jvmdg(projects.commonImplDeps.jvmdg)

    subproject(projects.injectModMetadata)
    subproject(projects.serviceProviders.log)
    subproject(projects.serviceProviders.lwjgl)
    //subproject(projects.serviceProviders.transformer)
    subproject(projects.serviceProviders.entrypoint.transformer)
    subproject(projects.serviceProviders.entrypoint.loader)

    moduleJar(projects.modules.main)
}

val currentJava = java.toolchain.languageVersion.map {
    JavaVersion.toVersion(it.asInt())
}

val mergeMainMeta = tasks.register<MergeMetaTask>("mergeMainMeta") {
    description = "Merges all MANIFEST.MF files from subprojects"
    mergeServices.set(false)
    sourceJars.from(subproject)
}
val mainJar = tasks.register<ShadowJar>("mainJar") {
    description = "Creates the mod jar including all subprojects"
    archiveAppendix.set(name)
    archiveClassifier.set("java${currentJava.get()}")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    configurations = listOf(embed)
    mergeMeta(mergeMainMeta)

    from(rootProject.layout.projectDirectory.file("LICENSE"))

    mergeServiceFiles()
    failOnDuplicateEntries = true
}

val mergeDepMeta = tasks.register<MergeMetaTask>("mergeDepMeta") {
    description = "Merges all MANIFEST.MF files from subprojects"
    mergeServices.set(false)
    sourceJars.from(subprojectDep)
}
val depJar = tasks.register<ShadowJar>("depJar") {
    description = "Creates a jar containing all to-be-embedded deps"
    archiveAppendix.set(name)
    archiveClassifier.set("java${currentJava.get()}")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    configurations = listOf(subprojectDep)
    mergeMeta(mergeDepMeta)

    mergeServiceFiles()
    failOnDuplicateEntries = true
}

val moduleJars = configurations.named(moduleJar.name)

// target java versions
listOf(
    JavaVersion.VERSION_25,
    JavaVersion.VERSION_21,
    JavaVersion.VERSION_17,
    JavaVersion.VERSION_1_8,
).forEach { javaVersion ->
    //val downgradedModuleJarsTask = downgradeJars(moduleJarsJavaCurrent, javaVersion)
    val mainJar = registerDowngradeJarTask(mainJar, javaVersion)
    val depJar = registerDowngradeJarTask(depJar, javaVersion)
    val bundleJarTask = registerBundleJarTask(
        moduleJars,
        mainJar,
        depJar,
        jvmdg,
        javaVersion
    )
    registerOutgoing(bundleJarTask, javaVersion)
    if (javaVersion == JavaVersion.VERSION_1_8) {
        configurations.runtimeElements {
            attributes {
                attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
            }
            outgoing.artifacts.clear()
            outgoing.artifact(bundleJarTask)
        }
    }
}

fun registerBundleJarTask(
    moduleJars: Provider<out FileCollection>,
    mainJar: TaskProvider<out Jar>,
    depJar: TaskProvider<out Jar>,
    jvmdg: Provider<Configuration>,
    javaVersion: JavaVersion
) = tasks.register<MergingJar>("bundleJarJava${javaVersion.majorVersion}") {
    group = "build"
    description = "Bundles the main jar, dep jar and all mpkmodules into a single jar."
    archiveAppendix.set("bundle")
    archiveClassifier.set("java${javaVersion.majorVersion}")
    duplicatesStrategy = DuplicatesStrategy.FAIL

    filesMatching("io/github/kurrycat/mpkmod/shaded/**") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    mergeJar(mainJar, depJar)
    mergeJars(jvmdg)
    into("mpkmodules") { from(moduleJars) }
}

fun registerDowngradeJarsTask(
    jars: Provider<out FileCollection>,
    javaVersion: JavaVersion,
): Provider<out FileCollection> {
    val major = javaVersion.majorVersion.toInt()

    val downgradedJars = tasks.register<DowngradeJars>("downgradeModulesToJava${major}") {
        description = "Downgrades the mpkmodule jars to Java $javaVersion"
        inputJars.from(jars)
        downgradeTo = javaVersion
    }

    val shadedJars = tasks.register<ShadeJars>("shadeDowngradedModulesJava${major}") {
        description = "Shades the downgraded to Java $javaVersion mpkmodules with their required java-api parts"
        inputJars.from(downgradedJars.map { it.outputDirectory.asFileTree })
        shadePath.set { "io/github/kurrycat/mpkmod/modules/shaded/" }
        downgradeTo = javaVersion
    }

    return shadedJars.map { it.outputDirectory.asFileTree }
}

abstract class SingleTaskTypeLock : BuildService<BuildServiceParameters.None>

val singleTaskTypeLock = gradle.sharedServices.registerIfAbsent(
    "singleTaskTypeLock",
    SingleTaskTypeLock::class.java
) {
    maxParallelUsages.set(1)
}

fun registerDowngradeJarTask(task: TaskProvider<out Jar>, javaVersion: JavaVersion): TaskProvider<out Jar> {
    if (javaVersion == currentJava.get()) {
        return task
    }

    val major = javaVersion.majorVersion.toInt()

    val downgradedJar = tasks.register<DowngradeJar>("downgrade${task.name.capitalized()}ToJava${major}") {
        description = "Downgrades the main jar to Java $javaVersion"
        inputFile.set(task.flatMap { it.archiveFile })
        downgradeTo.set(javaVersion)
        archiveAppendix.set(task.name)
        archiveClassifier.set("java$major")
        destinationDirectory.set(temporaryDir)
    }

    val shadedJar = tasks.register<ShadeJar>("shadeDowngraded${task.name.capitalized()}Java${major}Jar") {
        description = "Shades the downgraded to Java $javaVersion mod jar with its required java-api parts"
        inputFile.set(downgradedJar.flatMap { it.archiveFile })
        shadePath.set { "io/github/kurrycat/mpkmod/shaded/" }
        archiveAppendix.set(task.name)
        archiveClassifier.set("java$major-shaded")
        destinationDirectory.set(temporaryDir)
        //FIXME: remove after https://github.com/unimined/JvmDowngrader/issues/43 is fixed
        usesService(singleTaskTypeLock)
    }

    return shadedJar
}

fun registerOutgoing(task: TaskProvider<out Task>, javaVersion: JavaVersion) {
    configurations.register("${task.name}Outgoing") {
        isCanBeConsumed = true
        isCanBeResolved = false

        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, javaVersion.majorVersion.toInt())
        }

        outgoing.artifact(task)
    }

    tasks.assemble { dependsOn(task) }
}

fun Configuration.sources() = incoming.artifactView {
    @Suppress("UnstableApiUsage")
    withVariantReselection()
    attributes {
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
    }
}.files

val sourcesMergeMetaTask = tasks.register<MergeMetaTask>("sourcesMergeMetaTask") {
    description = "Merges all MANIFEST.MF and service files from subprojects"
    sourceJars.from(subproject)
}

tasks.sourcesJar {
    subproject.sources().apply {
        dependsOn(buildDependencies)
        from(map(::zipTree)) { excludeMeta() }
    }

    mergeMeta(sourcesMergeMetaTask)

    from(rootProject.layout.projectDirectory.file("LICENSE"))
}

tasks.jar { enabled = false }

listOf(
    configurations.apiElements,
    configurations.mainSourceElements,
).forEach {
    it {
        outgoing.artifacts.clear()
        outgoing.variants.clear()
    }
}