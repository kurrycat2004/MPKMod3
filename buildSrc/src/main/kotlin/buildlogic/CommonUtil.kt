package buildlogic

import groovy.json.JsonOutput
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.named

// TODO: Use -Xcontext-parameters
fun SourceSet.compileOnly(dependencies: DependencyHandlerScope, vararg deps: Any) {
    deps.forEach { dependencies.add(compileOnlyConfigurationName, it) }
}

fun SourceSet.annotationProcessor(dependencies: DependencyHandlerScope, vararg deps: Any) {
    deps.forEach { dependencies.add(annotationProcessorConfigurationName, it) }
}


fun <K, V> json(vararg pairs: Pair<K, V>): String =
    JsonOutput.prettyPrint(JsonOutput.toJson(mapOf(*pairs)))

inline fun <reified T : Task> tasks(projects: List<Project>, name: String) =
    projects.map { it.tasks.named<T>(name) }

fun jars(jarTasks: List<TaskProvider<Jar>>) =
    jarTasks.map { it.flatMap(Jar::getArchiveFile) }