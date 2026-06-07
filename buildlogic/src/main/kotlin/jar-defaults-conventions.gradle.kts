import buildlogic.ModMetadata
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false

    val modMetadataFile = rootProject.layout.projectDirectory.file("mod-metadata.toml")
    val modMetadata = ModMetadata.read(modMetadataFile.asFile.toPath())
    archiveBaseName = modMetadata.id()

    eachFile {
        permissions {
            val isExec = Files.getPosixFilePermissions(file.toPath())
                .contains(PosixFilePermission.OWNER_EXECUTE)
            unix(if (isExec) "755" else "644")
        }
    }
    dirPermissions { unix("755") }
}