package buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar
import xyz.wagyourtail.jvmdg.gradle.task.ShadeJar

fun Project.registerDowngradedJvmVariant(version: JavaVersion, classpath: Provider<FileCollection>) {
    plugins.apply("xyz.wagyourtail.jvmdowngrader")

    val major = version.majorVersion.toInt()

    val downgradedJar = tasks.register<DowngradeJar>("downgradeToJava${major}") {
        val inputJarTask = tasks.named<Jar>("jar")
        inputFile.set(inputJarTask.flatMap { it.archiveFile })
        downgradeTo.set(version)
        this.classpath = classpath.get()
        archiveClassifier.set("java$major")
    }

    val shadedJar = tasks.register<ShadeJar>("shadeDowngradedJava${major}Jar") {
        inputFile.set(downgradedJar.flatMap { it.archiveFile })
        shadePath.set { "io/github/kurrycat/mpkmod/shaded" }
        archiveClassifier.set("java$major-shaded")
    }

    configurations.register("downgradedJava${major}") {
        isCanBeConsumed = true
        isCanBeResolved = false

        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, major)
        }

        outgoing.artifact(shadedJar)
    }
}