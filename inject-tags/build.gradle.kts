import buildlogic.ModMetadata

plugins {
    id("jar-defaults-conventions")
}

abstract class InjectTagsTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val metadataFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        fun escapeJava(value: String): String =
            value.replace("\\", "\\\\").replace("\"", "\\\"")

        val meta = ModMetadata.read(metadataFile.get().asFile.toPath())

        val className = "Tags"

        val outFile = outputDir.get().asFile
            .resolve(meta.group.replace('.', '/'))
            .resolve("$className.java")

        outFile.parentFile.mkdirs()

        outFile.writeText(
            """
            package ${meta.group};

            /** Auto-generated tags class - DO NOT MODIFY */
            public final class $className {
                private $className() {}

                public static final String MOD_ID = "${escapeJava(meta.id)}";
                public static final String MOD_NAME = "${escapeJava(meta.name)}";
                public static final String MOD_GROUP = "${escapeJava(meta.group)}";
                public static final String MOD_VERSION = "${escapeJava(meta.version)}";
            }
            """.trimIndent() + "\n"
        )
    }
}

val generatedTagsDir = layout.buildDirectory.dir("generated/sources/injectTags")

val injectTags by tasks.registering(InjectTagsTask::class) {
    group = "build"
    description = "Generates the Tags.java class"
    metadataFile.set(rootProject.layout.projectDirectory.file("mod-metadata.toml"))
    outputDir.set(generatedTagsDir)
}

sourceSets.main {
    java.srcDir(generatedTagsDir)
}

tasks.compileJava {
    dependsOn(injectTags)
}

tasks.sourcesJar {
    dependsOn(injectTags)
}

dependencies {
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)

    compileOnly(projects.commonApi)
}