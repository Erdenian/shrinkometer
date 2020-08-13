import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.12.0"
}

group = "ru.erdenian"
version = "0.1.0"

repositories {
    google()
    jcenter()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.android.tools.build:gradle:4.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.1")
}

pluginBundle {
    website = "https://github.com/Erdenian/shrinkometer"
    vcsUrl = "https://github.com/Erdenian/shrinkometer.git"
    tags = listOf("java", "kotlin", "android", "proguard", "r8", "shrink", "minify", "statistics")
}

gradlePlugin {
    plugins {
        create("shrinkometer") {
            id = "ru.erdenian.shrinkometer"
            displayName = "Calculate size savings after shrinkage"
            description = "Calculates per class size difference in dex files with and without code shrinking"
            implementationClass = "ru.erdenian.shrinkometer.gradle.ProGuardStatisticsPlugin"
        }
    }
}
