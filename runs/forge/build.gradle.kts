import buildlogic.RunConfiguration
import buildlogic.projectName
import net.minecraftforge.gradle.ForgeGradleExtension
import net.minecraftforge.gradle.MinecraftExtension
import net.minecraftforge.gradle.MinecraftExtensionForProject
import net.minecraftforge.gradle.shadow.net.minecraftforge.gradleutils.shared.ToolsExtension
import java.util.jar.JarFile

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

        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion = JavaLanguageVersion.of(forge.java)
        }

        repositories {
            mavenLocal()
            extensions.getByType<MinecraftExtension>().mavenizer(this)
            maven(extensions.getByType<ForgeGradleExtension>().forgeMaven)
            maven(extensions.getByType<ForgeGradleExtension>().minecraftLibsMaven)
            maven("https://cursemaven.com")
        }

        //TODO: remove after https://github.com/MinecraftForge/MinecraftMavenizer/pull/29 is merged
        extensions.configure<ToolsExtension> {
            configure("slimelauncher") {
                version = "0.2.1"
            }
            configure("mavenizer") {
                version = "0.5.10"
            }
        }

        extensions.configure<MinecraftExtension> {
            mappings(forge.mappings.channel, forge.mappings.version)
        }

        extensions.configure<MinecraftExtensionForProject> {
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
                    jvmArgs("-Dmpkmod.service.logProviders=true")
                    jvmArgs("-Dmpkmod.module.enableModuleLoadStacktrace=true")

                    val coreMods = mutableListOf<String>()
                    configurations["runtimeClasspath"].forEach {
                        if (it.extension != "jar") return@forEach
                        val manifest = JarFile(it).manifest ?: return@forEach
                        val coreMod = manifest.mainAttributes.getValue("FMLCorePlugin") ?: return@forEach
                        coreMods.add(coreMod)
                    }
                    jvmArgs("-Dfml.coreMods.load=${coreMods.joinToString(",")}")
                }
                /*register("server") {
                    args("--nogui", "fml_bug", "--port", 25565 + 189)
                    workingDir = file("../run/server/")
                }*/
            }
        }

        /*extensions.configure<RenamerExtension> {
            mappings.from(extensions.getByType<MinecraftExtensionForProject>().dependency.toSrgFile)

            dependencies {
                add(
                    "runtimeOnly",
                    dependency("curse.maven:jei-238222:5846804")
                )
            }
        }*/
    }
}
