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

tasks.jar { enabled = false }

val javaApiShadowJar = tasks.register<ShadowJar>("javaApiShadowJar") {
    group = "jvmdowngrader"
    archiveBaseName = "java-api"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    failOnDuplicateEntries = true

    configurations = listOf(jvmdgJavaApi)
    exclude("module-info.class")
    relocate("org.objectweb.asm", "xyz.wagyourtail.jvmdg.shade.asm")
}

val jvmdgShadowJar = tasks.register<ShadowJar>("jvmdgShadowJar") {
    group = "jvmdowngrader"
    archiveBaseName = "jvmdowngrader"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    failOnDuplicateEntries = true

    configurations = listOf(jvmdowngrader)
    exclude("module-info.class")
    relocate("org.objectweb.asm", "xyz.wagyourtail.jvmdg.shade.asm")
    /*minimize {
        include(dependency("org.ow2.asm:.*:.*"))
    }*/
}

val mergeMetaTask = tasks.register<MergeMetaTask>("mergeMetaTask") {
    mergeServices.set(false)
    sourceJars.from(subproject)
}

val shadowJarJava21 = tasks.register<ShadowJar>("shadowJarJava21") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    configurations = listOf(embed)

    from(javaApiShadowJar.map { it.archiveFile }) {
        into("META-INF/lib")
        rename { "java-api.jar" }
    }

    mergeMeta(mergeMetaTask)

    mergeServiceFiles()
    failOnDuplicateEntries = true
}

val moduleJarsJava21 = configurations.named(moduleJar.name)
val bundleJarJava21 = registerBundleJar(
    moduleJarsJava21,
    shadowJarJava21,
    jvmdgShadowJar,
    JavaVersion.VERSION_21
)
registerOutgoing(bundleJarJava21, JavaVersion.VERSION_21)

val moduleJarsJava8 = downgradeJars(moduleJarsJava21, JavaVersion.VERSION_1_8)
val shadowJarJava8 = downgradeTask(shadowJarJava21, JavaVersion.VERSION_1_8)
val bundleJarJava8 = registerBundleJar(
    moduleJarsJava21,
    shadowJarJava8,
    jvmdgShadowJar,
    JavaVersion.VERSION_1_8
)
registerOutgoing(bundleJarJava8, JavaVersion.VERSION_1_8)

fun registerBundleJar(
    modules: Provider<out FileCollection>,
    mainJar: TaskProvider<out Jar>,
    jvmdgJar: TaskProvider<out Jar>,
    javaVersion: JavaVersion
) = tasks.register<MergingJar>("bundleJarJava${javaVersion.majorVersion}") {
    group = "build"
    archiveAppendix.set("bundle")
    archiveClassifier.set("java${javaVersion.majorVersion}")

    mergeJar(mainJar, jvmdgJar)
    into("mpkmodules") { from(modules) }
}

fun downgradeJars(jars: Provider<out FileCollection>, javaVersion: JavaVersion): Provider<out FileCollection> {
    val major = javaVersion.majorVersion.toInt()

    val downgradedJars = tasks.register<DowngradeJars>("downgradeModulesToJava${major}") {
        inputJars.from(jars)
        downgradeTo = javaVersion
    }

    val shadedJars = tasks.register<ShadeJars>("shadeDowngradedModulesJava${major}") {
        inputJars.from(downgradedJars.map { it.outputDirectory.asFileTree })
        shadePath.set { "io/github/kurrycat/mpkmod/modules/shaded/" }
        downgradeTo = javaVersion
    }

    return shadedJars.map { it.outputDirectory.asFileTree }
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
}

fun Configuration.sources() = incoming.artifactView {
    withVariantReselection()
    attributes {
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
    }
}.files

val sourcesMergeMetaTask = tasks.register<MergeMetaTask>("sourcesMergeMetaTask") {
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
