import buildlogic.RunConfiguration
import buildlogic.projectName
import net.minecraftforge.gradle.ForgeGradleExtension
import net.minecraftforge.gradle.MinecraftExtension
import net.minecraftforge.gradle.MinecraftExtensionForProject

plugins {
    alias(libs.plugins.forge.gradle) apply false
    alias(libs.plugins.forge.renamer) apply false
}

val forgeGradlePluginId = libs.plugins.forge.gradle.get().pluginId;
val forgeRenamerPluginId = libs.plugins.forge.renamer.get().pluginId;
val bundleProject = projects.bundle

val runConfigurationFile = file("../run-configuration.toml")
val runConfigurations: RunConfiguration = RunConfiguration.read(runConfigurationFile.toPath())

runConfigurations.forge.forEach { forge ->
    project(":runs:forge:${forge.projectName()}") {
        pluginManager.apply("java")
        pluginManager.apply(forgeGradlePluginId)
        pluginManager.apply(forgeRenamerPluginId)

        configure<JavaPluginExtension> {
            toolchain.languageVersion = JavaLanguageVersion.of(forge.java)
        }

        repositories {
            mavenLocal()
            the<MinecraftExtension>().mavenizer(this)
            maven(the<ForgeGradleExtension>().forgeMaven)
            maven(the<ForgeGradleExtension>().minecraftLibsMaven)
            maven("https://cursemaven.com")
        }

        configure<MinecraftExtension> {
            mappings(forge.mappings.channel, forge.mappings.version)
        }

        configure<MinecraftExtensionForProject> {
            dependencies {
                add(
                    "implementation",
                    dependency("net.minecraftforge:forge:${forge.minecraft}-${forge.forge}")
                )
                add("runtimeOnly", bundleProject)
            }

            runs {
                register("client") {
                    workingDir = file("../run/client/")
                    jvmArgs("-Dfml.coreMods.load=io.github.kurrycat.mpkmod.transformer.fml.FMLLoadingPlugin")

                    jvmArgs("-Dmpkmod.logger.mpkmod=DEBUG")
                    jvmArgs("-Dmpkmod.enable_module_load_stacktrace=true")

                    //jvmArgs("-Djvmdg.debug.dumpClasses=true")
                    //jvmArgs("-Djvmdg.debug=true")

                    val runtimeClasspath = configurations.named("runtimeClasspath")
                    val bundle = runtimeClasspath.map {
                        it.incoming.artifacts.artifacts.forEach { f -> println(f) }
                        it.incoming.artifacts.artifacts.find { a ->
                            val id = a.id.componentIdentifier
                            id is ProjectComponentIdentifier && id.projectPath == bundleProject.path
                        }!!.let { a ->
                            mapOf("MOD_CLASSES" to a.file.path)
                        }
                    }

                    environment(bundle)
                }
                /*register("server") {
                    args("--nogui", "fml_bug", "--port", 25565 + 189)
                    workingDir = file("../run/server/")
                }*/
            }
        }

        /*configure<RenamerExtension> {
            mappings.from(the<MinecraftExtensionForProject>().dependency.toSrgFile)

            dependencies {
                add(
                    "runtimeOnly",
                    dependency("curse.maven:jei-238222:5846804")
                )
            }
        }*/
    }
}
