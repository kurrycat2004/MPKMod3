plugins {
    id("jar-defaults-conventions")
}

dependencies {
    api(projects.commonApiDeps)
    compileOnlyApi(libs.jetbrains.annotations)
}