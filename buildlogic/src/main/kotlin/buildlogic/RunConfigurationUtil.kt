package buildlogic

fun RunConfiguration.Fabric.projectName() = "mc-${minecraft.replace('.', '-')}"
fun RunConfiguration.Forge.projectName() = "mc-${minecraft.replace('.', '-')}"