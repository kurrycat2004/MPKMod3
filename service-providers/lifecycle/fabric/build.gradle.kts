import buildlogic.GenerateModMetadata
import buildlogic.json

plugins {
    id("jar-defaults-conventions")
}

val generateFabricModJson by tasks.registering(GenerateModMetadata::class) {
    description = "Gemerate fabric.mod.json from mod metadata"
    outputDir.set(layout.buildDirectory.dir("generated/resources/fabric"))
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
            "icon" to meta.logoFile(),
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

sourceSets.main {
    resources.srcDir(generateFabricModJson.map { it.outputDir })
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)
    compileOnly(projects.commonApi)

    compileOnly(libs.fabric.loader)
}