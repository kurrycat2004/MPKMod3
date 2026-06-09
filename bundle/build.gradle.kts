import buildlogic.DowngradeJars
import buildlogic.MergeMetaTask
import buildlogic.MergingJar
import buildlogic.ShadeJars
import buildlogic.excludeMeta
import buildlogic.mergeMeta
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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

val subprojectImplementation = configurations.create("subprojectImplementation") {
    configurations.implementation { extendsFrom(this@create) }
    configurations.named(subproject.name) { extendsFrom(this@create) }
}

val subprojectApi = configurations.create("subprojectApi") {
    configurations.api { extendsFrom(this@create) }
    configurations.named(subproject.name) { extendsFrom(this@create) }
}

val embedLibApi = configurations.create("embedLibApi") {
    configurations.api { extendsFrom(this@create) }
    configurations.named(embed.name) { extendsFrom(this@create) }
}

val jvmdowngrader = configurations.create("jvmdowngrader")

repositories {
    mavenCentral()
    maven("https://maven.wagyourtail.xyz/snapshots/")
}

dependencies {
    subprojectApi(projects.commonApi)

    subprojectImplementation(projects.injectModMetadata)
    subprojectImplementation(projects.commonImpl)
    subprojectImplementation(projects.serviceProviders.log)
    subprojectImplementation(projects.serviceProviders.lwjgl)
    //implementation(projects.serviceProviders.transformer)
    subprojectImplementation(projects.serviceProviders.entrypoint.transformer)
    subprojectImplementation(projects.serviceProviders.entrypoint.loader)

    moduleJar(projects.modules.main)

    jvmdgJavaApi(libs.jvmdowngrader.java.api) {
        artifact { classifier = "downgraded-8" }
    }

    jvmdowngrader(libs.jvmdowngrader)
    compileOnly(libs.jvmdowngrader) {
        exclude(group = "org.ow2.asm", module = "asm")
        exclude(group = "org.ow2.asm", module = "asm-tree")
        exclude(group = "org.ow2.asm", module = "asm-commons")
        exclude(group = "org.ow2.asm", module = "asm-util")
    }

    embedLibApi(libs.jtoml)
}

val currentJava = java.toolchain.languageVersion.map {
    JavaVersion.toVersion(it.asInt())
}

tasks.jar { enabled = false }

val jvmdgJavaApiJar = tasks.register<ShadowJar>("jvmdgJavaApiJar") {
    group = "jvmdowngrader"
    description = "Creates the java-api jar for jvmdg with relocated asm."
    archiveBaseName = "java-api"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    failOnDuplicateEntries = true

    configurations = listOf(jvmdgJavaApi)
    exclude("module-info.class")
    relocate("org.objectweb.asm", "xyz.wagyourtail.jvmdg.shade.asm")
}

val jvmdgJar = tasks.register<ShadowJar>("jvmdgJar") {
    group = "jvmdowngrader"
    description = "Creates the jvmdg library jar with relocated asm."
    archiveBaseName = "jvmdowngrader"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    failOnDuplicateEntries = true

    configurations = listOf(jvmdowngrader)

    from(jvmdgJavaApiJar.map { it.archiveFile }) {
        into("META-INF/lib")
        rename { "java-api.jar" }
    }

    exclude("module-info.class")
    relocate("org.objectweb.asm", "xyz.wagyourtail.jvmdg.shade.asm")
    /*minimize {
        include(dependency("org.ow2.asm:.*:.*"))
    }*/
}

val mergeSubprojectMeta = tasks.register<MergeMetaTask>("mergeSubprojectMeta") {
    description = "Merges all MANIFEST.MF files from subprojects"
    mergeServices.set(false)
    sourceJars.from(subproject)
}

val shadowJar = tasks.register<ShadowJar>("shadowJar") {
    description = "Creates the mod jar including all subprojects and embedded libs (excluding jvmdg)."
    archiveClassifier.set("java${currentJava.get()}")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    configurations = listOf(embed)

    mergeMeta(mergeSubprojectMeta)

    mergeServiceFiles()
    failOnDuplicateEntries = true
}

val moduleJars = configurations.named(moduleJar.name)

// target java versions
listOf(
    JavaVersion.VERSION_21,
    JavaVersion.VERSION_17,
    JavaVersion.VERSION_1_8,
).forEach { javaVersion ->
    //val downgradedModuleJarsTask = downgradeJars(moduleJarsJavaCurrent, javaVersion)
    val mainJar = if (javaVersion == currentJava.get()) {
        shadowJar
    } else {
        registerDowngradeJarTask(shadowJar, javaVersion)
    }
    val bundleJarTask = registerBundleJarTask(
        moduleJars,
        mainJar,
        jvmdgJar,
        javaVersion
    )
    registerOutgoing(bundleJarTask, javaVersion)
}

fun registerBundleJarTask(
    moduleJars: Provider<out FileCollection>,
    mainJar: TaskProvider<out Jar>,
    jvmdgJar: TaskProvider<out Jar>,
    javaVersion: JavaVersion
) = tasks.register<MergingJar>("bundleJarJava${javaVersion.majorVersion}") {
    group = "build"
    description = "Bundles the main jar, jvmdg jar and all mpkmodules into a single jar."
    archiveAppendix.set("bundle")
    archiveClassifier.set("java${javaVersion.majorVersion}")

    mergeJar(mainJar, jvmdgJar)
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

fun registerDowngradeJarTask(task: TaskProvider<out Jar>, javaVersion: JavaVersion): TaskProvider<out Jar> {
    val major = javaVersion.majorVersion.toInt()

    val downgradedJar = tasks.register<DowngradeJar>("downgradeToJava${major}") {
        description = "Downgrades the main jar to Java $javaVersion"
        inputFile.set(task.flatMap { it.archiveFile })
        downgradeTo.set(javaVersion)
        archiveClassifier.set("java$major")
    }

    val shadedJar = tasks.register<ShadeJar>("shadeDowngradedJava${major}Jar") {
        description = "Shades the downgraded to Java $javaVersion mod jar with its required java-api parts"
        inputFile.set(downgradedJar.flatMap { it.archiveFile })
        shadePath.set { "io/github/kurrycat/mpkmod/shaded/" }
        archiveClassifier.set("java$major-shaded")
    }

    return shadedJar
}

fun registerOutgoing(task: TaskProvider<out Task>, javaVersion: JavaVersion) {
    configurations.register("${task.name}Outgoing") {
        isCanBeConsumed = true
        isCanBeResolved = false

        attributes {
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
