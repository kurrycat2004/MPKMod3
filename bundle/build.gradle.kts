import buildlogic.MergeMetaTask
import buildlogic.MergingJar
import buildlogic.mergeMeta
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar
import xyz.wagyourtail.jvmdg.gradle.task.ShadeJar
import xyz.wagyourtail.jvmdg.gradle.task.files.DowngradeFiles
import xyz.wagyourtail.jvmdg.gradle.task.files.ShadeFiles

plugins {
    id("jar-defaults-conventions")
    alias(libs.plugins.jvmdowngrader)
}

val moduleJar by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val embed by configurations.creating
configurations.implementation {
    extendsFrom(embed)
}

val minimizeDep by configurations.creating

repositories {
    mavenCentral()
    maven("https://maven.wagyourtail.xyz/snapshots/")
}

dependencies {
    api(projects.commonApi)

    embed(projects.injectModMetadata)
    embed(projects.commonImpl)
    embed(projects.serviceProviders.log)
    embed(projects.serviceProviders.lwjgl)
    //implementation(projects.serviceProviders.transformer)
    embed(projects.serviceProviders.entrypoint.transformer)
    embed(projects.serviceProviders.entrypoint.loader)

    moduleJar(projects.modules.main)

    minimizeDep(libs.jvmdowngrader)
    compileOnly(libs.jvmdowngrader) {
        exclude(group = "org.ow2.asm", module = "asm")
        exclude(group = "org.ow2.asm", module = "asm-tree")
        exclude(group = "org.ow2.asm", module = "asm-commons")
        exclude(group = "org.ow2.asm", module = "asm-util")
    }

    api(libs.jtoml)
}

tasks.jar { enabled = false }

val embedManifestFiles = providers.provider {
    embed.flatMap {
        zipTree(it).matching { include("META-INF/MANIFEST.MF") }
    }
}

val mergeMetaTask by tasks.registering(MergeMetaTask::class) {
    mergeServices.set(false)
    sourceJars.from(embed)
}

val shadowJar by tasks.registering(ShadowJar::class) {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    exclude("module-info.class")

    configurations = project.configurations.runtimeClasspath.map { listOf(it, minimizeDep) }

    relocate("org.objectweb.asm", "xyz.wagyourtail.jvmdg.shade.org.objectweb.asm")
    minimize { include(dependency(minimizeDep)) }

    mergeMeta(mergeMetaTask)

    mergeServiceFiles()
    failOnDuplicateEntries = true
}

val moduleJarsJava21 = providers.provider { moduleJar }
val bundleJarJava21 = registerBundleJar(moduleJarsJava21, shadowJar, JavaVersion.VERSION_21)
registerOutgoing(bundleJarJava21, JavaVersion.VERSION_21)

val moduleJarsJava8 = downgradeJars(moduleJarsJava21, JavaVersion.VERSION_1_8)
val shadowJarJava8 = downgradeTask(shadowJar, JavaVersion.VERSION_1_8)
val bundleJarJava8 = registerBundleJar(moduleJarsJava8, shadowJarJava8, JavaVersion.VERSION_1_8)
registerOutgoing(bundleJarJava8, JavaVersion.VERSION_1_8)

fun registerBundleJar(
    modules: Provider<out FileCollection>,
    mainJar: TaskProvider<out Jar>,
    javaVersion: JavaVersion
) = tasks.register<MergingJar>("bundleJarJava${javaVersion.majorVersion}") {
    group = "build"
    archiveAppendix.set("bundle")
    archiveClassifier.set("java${javaVersion.majorVersion}")

    mergeJar(mainJar)
    into("mpkmodules") { from(modules) }
}

fun downgradeJars(jars: Provider<out FileCollection>, javaVersion: JavaVersion): Provider<out FileCollection> {
    val major = javaVersion.majorVersion.toInt()

    val downgradedJars = tasks.register<DowngradeFiles>("downgradeModulesToJava${major}") {
        dependsOn(jars)
        inputCollection = jars.get()
        downgradeTo = javaVersion
    }

    val shadedJars = tasks.register<ShadeFiles>("shadeDowngradedModulesJava${major}") {
        dependsOn(downgradedJars)
        inputCollection = downgradedJars.get().outputCollection
        downgradeTo = javaVersion
    }

    return shadedJars.map { it.outputCollection }
}

fun downgradeTask(task: TaskProvider<out Jar>, javaVersion: JavaVersion): TaskProvider<out Jar> {
    val major = javaVersion.majorVersion.toInt()

    val downgradedJar = tasks.register<DowngradeJar>("downgradeToJava${major}") {
        inputFile.set(task.flatMap { it.archiveFile })
        downgradeTo.set(javaVersion)
        archiveClassifier.set("java$major")
    }

    val shadedJar = tasks.register<ShadeJar>("shadeDowngradedJava${major}Jar") {
        inputFile.set(downgradedJar.flatMap { it.archiveFile })
        shadePath.set { "io/github/kurrycat/mpkmod/shaded" }
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
}

/*
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.WARN

    tasks<Jar>(components, "jar").let {
        dependsOn(it)
        from(jars(it).map(::zipTree)) { excludeMeta() }
    }

    tasks<Jar>(modules, "jar").let {
        dependsOn(it)
        from(jars(it)) {
            into("mpkmodules")
        }
    }

    from(minimize.resolve().map(::zipTree))

    mergeMeta(mergeMetaTask)

    from(rootProject.layout.projectDirectory.file("LICENSE"))
}

registerDowngradedJvmVariant(
    JavaVersion.VERSION_1_8,
    sourceSets.main.map { it.compileClasspath }
)*/

/*
tasks.sourcesJar {
    tasks<Jar>(components, "sourcesJar").let {
        dependsOn(it)
        from(jars(it).map(::zipTree)) { excludeMeta() }
    }

    mergeMeta(mergeMetaTask)

    from(rootProject.layout.projectDirectory.file("LICENSE"))
}*/
