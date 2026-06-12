import buildlogic.GenerateModMetadata

plugins {
    id("jar-defaults-conventions")
}

val generateForgeModToml by tasks.registering(GenerateModMetadata::class) {
    description = "Gemerate mods.toml from mod metadata"
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

val stubs = sourceSets.create("stubs")
sourceSets.main {
    compileClasspath += stubs.output
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)
    compileOnly(projects.commonApi)

    compileOnly(projects.injectModMetadata)
    implementation(projects.serviceProviders.lifecycle.forgeCommon)
}