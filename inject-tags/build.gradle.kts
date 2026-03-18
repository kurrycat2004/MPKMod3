import buildlogic.prop

plugins {
    id("jar-defaults-conventions")
}

abstract class InjectTagsTask : DefaultTask() {
    @get:Input
    abstract val modGroup: Property<String>

    @get:Input
    abstract val modId: Property<String>

    @get:Input
    abstract val modName: Property<String>

    @get:Input
    abstract val modVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        fun escapeJava(value: String): String =
            value.replace("\\", "\\\\").replace("\"", "\\\"")

        val packageName = modGroup.get()
        val className = "Tags"

        val outFile = outputDir.get().asFile
            .resolve(packageName.replace('.', '/'))
            .resolve("$className.java")

        outFile.parentFile.mkdirs()

        outFile.writeText(
            """
            package $packageName;

            /** Auto-generated tags class - DO NOT MODIFY */
            public final class $className {
                private $className() {}

                public static final String MOD_ID = "${escapeJava(modId.get())}";
                public static final String MOD_NAME = "${escapeJava(modName.get())}";
                public static final String MOD_GROUP = "${escapeJava(modGroup.get())}";
                public static final String MOD_VERSION = "${escapeJava(modVersion.get())}";
            }
            """.trimIndent() + "\n"
        )
    }
}

val generatedTagsDir = layout.buildDirectory.dir("generated/sources/injectTags")

val injectTags by tasks.registering(InjectTagsTask::class) {
    group = "build"
    description = "Generates the Tags.java class"

    modGroup.set(prop("modGroup"))
    modId.set(prop("modId"))
    modName.set(prop("modName"))
    modVersion.set(prop("modVersion"))
    outputDir.set(generatedTagsDir)
}

sourceSets {
    named("main") {
        java.srcDir(generatedTagsDir)
    }
}

tasks.named("compileJava") {
    dependsOn(injectTags)
}

tasks.named("sourcesJar") {
    dependsOn(injectTags)
}