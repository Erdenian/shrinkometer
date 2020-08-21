import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.72"
    id("io.gitlab.arturbosch.detekt") version "1.11.1"
    id("com.gradle.plugin-publish") version "0.12.0"
}

group = "ru.erdenian"
version = "0.3.0"

repositories {
    google()
    jcenter()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")

    implementation("com.android.tools.build:gradle:4.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.1")
}

detekt {
    config = files("detekt-config.yml")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

System.getenv("GRADLE_PUBLISH_KEY")?.let { project.ext["gradle.publish.key"] = it }
System.getenv("GRADLE_PUBLISH_SECRET")?.let { project.ext["gradle.publish.secret"] = it }

pluginBundle {
    website = "https://github.com/Erdenian/shrinkometer"
    vcsUrl = "https://github.com/Erdenian/shrinkometer.git"
    tags = listOf("java", "kotlin", "android", "proguard", "r8", "shrink", "minify", "statistics")
}

gradlePlugin {
    plugins {
        create("shrinkometer") {
            id = "ru.erdenian.shrinkometer"
            implementationClass = "ru.erdenian.shrinkometer.gradle.ShrinkometerPlugin"

            displayName = "Calculate size savings after shrinkage"
            description = "Calculates per class size difference in dex files with and without code shrinking"
        }
    }
}
