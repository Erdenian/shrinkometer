plugins {
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.16.0")
}

detekt {
    config = files("detekt-config.yml")
}
