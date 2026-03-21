plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.jar {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}