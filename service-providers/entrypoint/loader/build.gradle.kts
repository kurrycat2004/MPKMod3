import buildlogic.GenerateModMetadata
import buildlogic.annotationProcessor
import buildlogic.compileOnly
import buildlogic.json

plugins {
    id("jar-defaults-conventions")
}

val generateFabricModJson by tasks.registering(GenerateModMetadata::class) {
    relativeOutputPath.set("fabric.mod.json")

    content.set(metadata.map { meta ->
        json(
            "schemaVersion" to 1,
            "id" to meta.id(),
            "version" to meta.version(),
            "name" to meta.name(),
            "description" to meta.description(),
            "authors" to meta.authors(),
            "contact" to buildMap {
                meta.homepage()?.takeIf { it.isNotBlank() }?.let { put("homepage", it) }
                meta.sources()?.takeIf { it.isNotBlank() }?.let { put("sources", it) }
                meta.issues()?.takeIf { it.isNotBlank() }?.let { put("issues", it) }
            },
            "license" to meta.license(),
            "icon" to "assets/mpkmod/logo_x256.png",
            "environment" to "client",
            "entrypoints" to mapOf(
                "main" to listOf(
                    "io.github.kurrycat.mpkmod.loader.fabric.FabricEntrypoint"
                )
            ),
            //TODO: collect all mixins dynamically
            "mixins" to listOf(
                "mixins.mpkmod.core.json"
            ),
            "depends" to mapOf(
                "fabricloader" to ">=${libs.versions.fabric.loader.get()}",
                "minecraft" to "*",
                "java" to ">=21"
            )
        )
    })
}

val fabric by sourceSets.creating {
    resources.srcDir(generateFabricModJson.map { it.outputDir })
}

val variants = listOf(fabric)

dependencies {
    fabric.compileOnly(this, libs.auto.service.annotations)
    fabric.annotationProcessor(this, libs.auto.service)

    fabric.compileOnly(this, projects.commonApi)
    fabric.compileOnly(this, libs.fabric.loader)
}

tasks.jar {
    from(variants.map { it.output })
}

tasks.named<Jar>("sourcesJar") {
    from(variants.map { it.allSource })
}