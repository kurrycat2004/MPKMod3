import buildlogic.ModMetadata

plugins {
    id("jar-defaults-conventions")
}

abstract class InjectModMetadata : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val metadataFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        fun escape(value: String): String = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")

        val meta = ModMetadata.read(metadataFile.get().asFile.toPath())

        val className = "ModMetadata"

        val outFile = outputDir.get().asFile
            .resolve(meta.group.replace('.', '/'))
            .resolve("$className.java")

        outFile.parentFile.mkdirs()

        outFile.writeText(
            """
            package ${meta.group};

            /** Auto-generated metadata class - DO NOT MODIFY */
            public final class $className {
                private $className() {}

                public static final String MOD_ID          = "${escape(meta.id)}";
                public static final String MOD_NAME        = "${escape(meta.name)}";
                public static final String MOD_GROUP       = "${escape(meta.group)}";
                public static final String MOD_VERSION     = "${escape(meta.version)}";
                public static final String MOD_DESCRIPTION = "${escape(meta.description)}";
                public static final String MOD_LICENSE     = "${escape(meta.license)}";
                public static final String MOD_LOGO_FILE   = "${escape(meta.logoFile)}";
                public static final String MOD_AUTHORS     = "${escape(meta.authors.joinToString(", "))}";
                public static final String MOD_HOMEPAGE    = "${escape(meta.homepage)}";
                public static final String MOD_SOURCES     = "${escape(meta.sources)}";
                public static final String MOD_ISSUES      = "${escape(meta.issues)}";
            }
            """.trimIndent() + "\n"
        )
    }
}

val injectModMetadata by tasks.registering(InjectModMetadata::class) {
    group = "build"
    description = "Generates the Tags.java class"
    metadataFile.set(rootProject.layout.projectDirectory.file("mod-metadata.toml"))
    outputDir.set(layout.buildDirectory.dir("generated/sources/modmetadata"))
}

sourceSets.main {
    java.srcDir(injectModMetadata.map { it.outputDir })
}

tasks.compileJava {
    dependsOn(injectModMetadata)
}

tasks.sourcesJar {
    dependsOn(injectModMetadata)
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)

    compileOnly(projects.commonApi)
}