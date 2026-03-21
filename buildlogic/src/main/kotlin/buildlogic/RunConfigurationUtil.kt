package buildlogic

fun RunConfiguration.Fabric.projectName() = "mc-${version.replace('.', '-')}"