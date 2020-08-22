package ru.erdenian.shrinkometer.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.core.BuilderConstants
import com.android.builder.model.Version
import com.android.sdklib.tool.sdkmanager.SdkManagerCli
import java.io.File
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class ShrinkometerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val appExtensionClass = try {
            Class.forName("com.android.build.gradle.AppExtension") as Class<AppExtension>
        } catch (e: ClassNotFoundException) {
            throw GradleException("shrinkometer plugin must be used with com.android.application plugin")
        }

        val app = target.extensions
            .findByType(appExtensionClass)
            ?: throw GradleException("shrinkometer plugin must be used with com.android.application plugin")

        val apkAnalyserProvider = target.providers.provider {
            val cmdlineTools = File(app.sdkDirectory, "cmdline-tools")
            val bin = File(cmdlineTools, "latest|bin".replace('|', File.separatorChar))

            fun findAnalyzer() = bin.listFiles { _, name -> name.startsWith("apkanalyzer") }?.single()

            findAnalyzer() ?: run {
                SdkManagerCli.main(arrayOf("cmdline-tools;latest", "--sdk_root=${app.sdkDirectory}"))
                findAnalyzer()
            } ?: throw GradleException("Could not find apkanalyzer executable")
        }

        val pairFounder = DebugReleasePairFounder({ debug, release ->
            val capitalizedFlavorName = release.flavorName.capitalize()
            target.tasks.create(
                "shrinkometer$capitalizedFlavorName",
                ShrinkometerTask::class.java
            ) { task ->
                task.apkAnalyzerFile = apkAnalyserProvider
                task.reportFile = File(target.buildDir, "reports/shrinkometer/shrinkometer$capitalizedFlavorName.html")

                task.debugApkFile = debug.outputs.single().outputFile
                task.releaseApkFile = release.outputs.single().outputFile
                task.mappingFile = if (release.buildType.isMinifyEnabled) {
                    if (Version.ANDROID_GRADLE_PLUGIN_VERSION < "3.6.0") release.mappingFile
                    else release.mappingFileProvider.get().singleFile
                } else null

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
                if (previous.buildType.name == buildTypeName) throw GradleException(
                    "More than one variant with build type '$buildTypeName' for flavor '${variant.flavorName}'"
                )
                map.remove(flavorName)

                if (buildTypeName == releaseName) onPairFound(previous, variant)
                else onPairFound(variant, previous)
            }
        }
    }
}
