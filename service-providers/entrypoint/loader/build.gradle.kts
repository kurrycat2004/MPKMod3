import buildlogic.GenerateModMetadata
import buildlogic.MergeMetaTask
import buildlogic.annotationProcessor
import buildlogic.compileOnly
import buildlogic.excludeMeta
import buildlogic.json
import buildlogic.mergeMeta

plugins {
    id("jar-defaults-conventions")
}

val generateFabricModJson by tasks.registering(GenerateModMetadata::class) {
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

val generateForgeModToml by tasks.registering(GenerateModMetadata::class) {
    outputDir.set(layout.buildDirectory.dir("generated/resources/forge"))
    relativeOutputPath.set("META-INF/mods.toml")

    content.set(metadata.map { meta ->
        """
        modLoader = "javafml"
        loaderVersion = "*"
        license = "${meta.license()}"
        issueTrackerURL = "${meta.issues()}"
        clientSideOnly = true
        
        [[mods]]
        modId = "${meta.id()}"
        version = "${meta.version()}"
        displayName = "${meta.name()}"
        description = "${meta.description()}"
        logoFile="${meta.logoFile()}"
        # updateJSONURL = ""
        authors = "${meta.authors().joinToString(", ")}"
        displayURL = "${meta.homepage()}"
    """.trimIndent()
    })
}

val fabric by sourceSets.creating {
    resources.srcDir(generateFabricModJson.map { it.outputDir })
}

val commonForge by sourceSets.creating

val forgeStubs by sourceSets.creating
val forge by sourceSets.creating {
    compileClasspath += forgeStubs.output + commonForge.output
    resources.srcDir(generateForgeModToml.map { it.outputDir })
}

val vintageForgeStubs by sourceSets.creating
val vintageForge by sourceSets.creating {
    compileClasspath += vintageForgeStubs.output + commonForge.output
}

val archaicForgeStubs by sourceSets.creating
val archaicForge by sourceSets.creating {
    compileClasspath += archaicForgeStubs.output + commonForge.output
}

val variants = listOf(
    fabric,
    commonForge,
    forge,
    vintageForge,
    archaicForge,
)

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    variants.forEach {
        it.compileOnly(this, libs.auto.service.annotations)
        it.annotationProcessor(this, libs.auto.service)

        it.compileOnly(this, projects.commonApi)
    }

    fabric.compileOnly(this, libs.fabric.loader)

    forge.compileOnly(this, projects.injectModMetadata)
}

val mergeMetaTask by tasks.registering(MergeMetaTask::class) {
    sourceRoots.from(variants.map { it.output })
}

tasks.jar {
    from(variants.map { it.output }) { excludeMeta() }
    mergeMeta(mergeMetaTask)
}

tasks.sourcesJar {
    from(variants.map { it.allSource }) { excludeMeta() }
    mergeMeta(mergeMetaTask)
}