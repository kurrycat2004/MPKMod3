package buildlogic

fun RunConfiguration.Fabric.projectName() = "mc-${mcVersion.replace('.', '-')}"
fun RunConfiguration.Forge.projectName() = "mc-${mcVersion.replace('.', '-')}"