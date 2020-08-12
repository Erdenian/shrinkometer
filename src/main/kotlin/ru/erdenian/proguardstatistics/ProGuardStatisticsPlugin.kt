package ru.erdenian.proguardstatistics

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.core.BuilderConstants
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class ProGuardStatisticsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val app = target.extensions
            .findByType(AppExtension::class.java)
            ?: throw GradleException("ProguardStatistics plugin must be used with android application plugin")

        val pairFounder = DebugReleasePairFounder({ debug, release ->
            val capitalizedFlavorName = release.flavorName.capitalize()
            target.tasks.create(
                "calculate${capitalizedFlavorName}ProGuardStatistics",
                CalculateProGuardStatisticsTask::class.java
            ) { task ->
                task.sdkPath = app.sdkDirectory.absolutePath
                task.reportFilePath = "./result.html"

                task.debugApkPath = debug.outputs.single().outputFile.absolutePath
                task.releaseApkPath = release.outputs.single().outputFile.absolutePath
                task.mappingFilePath = release.mappingFileProvider.get().singleFile.absolutePath

                task.dependsOn(
                    "assemble${capitalizedFlavorName}Debug",
                    "assemble${capitalizedFlavorName}Release"
                )
            }
        })

        app.applicationVariants.all(pairFounder::put)
    }

    private class DebugReleasePairFounder(
        private val onPairFound: (debug: ApplicationVariant, release: ApplicationVariant) -> Unit,
        private val debugName: String = BuilderConstants.DEBUG,
        private val releaseName: String = BuilderConstants.RELEASE
    ) {

        private val map = mutableMapOf<String, ApplicationVariant>()

        fun put(variant: ApplicationVariant) {
            val buildTypeName = variant.buildType.name
            if ((buildTypeName != debugName) && (buildTypeName != releaseName)) return

            val flavorName = variant.flavorName
            val previous = map[flavorName]

            if (previous == null) {
                map[flavorName] = variant
            } else {
                check(previous.buildType.name != buildTypeName) {
                    "More than one variant with build type '$buildTypeName' for flavor '${variant.flavorName}'"
                }
                map.remove(flavorName)

                if (buildTypeName == releaseName) onPairFound(previous, variant)
                else onPairFound(variant, previous)
            }
        }
    }
}
