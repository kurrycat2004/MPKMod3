plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
}

tasks.jar {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}