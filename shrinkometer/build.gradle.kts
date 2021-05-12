import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.5.0"
    id("com.gradle.plugin-publish") version "0.14.0"
}

group = "ru.erdenian"
version = "0.3.1"

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

repositories {
    google()
    jcenter()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")

    compileOnly("com.android.tools.build:gradle:3.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

// region Gradle Publish

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

// endregion
