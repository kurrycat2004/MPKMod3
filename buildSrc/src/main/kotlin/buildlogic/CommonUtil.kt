package buildlogic

import groovy.json.JsonOutput
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.DependencyHandlerScope

fun prop(providers: ProviderFactory, propertyName: String) =
    providers.gradleProperty(propertyName).orNull ?: error("Property $propertyName not found in gradle.properties")

fun Project.prop(propertyName: String): String = prop(providers, propertyName)
fun Settings.prop(propertyName: String): String = prop(providers, propertyName)

// TODO: Use -Xcontext-parameters
fun SourceSet.compileOnly(dependencies: DependencyHandlerScope, vararg deps: Any) {
    deps.forEach { dependencies.add(compileOnlyConfigurationName, it) }
}

fun SourceSet.annotationProcessor(dependencies: DependencyHandlerScope, vararg deps: Any) {
    deps.forEach { dependencies.add(annotationProcessorConfigurationName, it) }
}

fun <K, V> json(vararg pairs: Pair<K, V>): String =
    JsonOutput.prettyPrint(JsonOutput.toJson(mapOf(*pairs)))