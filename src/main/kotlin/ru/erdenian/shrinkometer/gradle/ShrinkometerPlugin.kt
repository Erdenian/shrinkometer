package ru.erdenian.shrinkometer.gradle

import com.android.SdkConstants
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.core.BuilderConstants
import com.android.sdklib.tool.sdkmanager.SdkManagerCli
import java.io.File
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class ShrinkometerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val app = target.extensions
            .findByType(AppExtension::class.java)
            ?: throw GradleException("shrinkometer plugin must be used with android application plugin")

        val apkAnalyserProvider = target.providers.provider {
            fun installTools() = SdkManagerCli.main(arrayOf("cmdline-tools;latest", "--sdk_root=${app.sdkDirectory}"))
            fun findAnalyzer(bin: File) = bin.listFiles { _, name -> name.startsWith("apkanalyzer") }?.single()

            val cmdlineTools = File(app.sdkDirectory, SdkConstants.FD_CMDLINE_TOOLS)
            val bin = File(cmdlineTools, "latest|bin".replace('|', File.separatorChar))

            var apkAnalyzer = findAnalyzer(bin)
            if (bin.exists().not() || (apkAnalyzer == null)) {
                installTools()
                apkAnalyzer = findAnalyzer(bin)
            }

            apkAnalyzer ?: throw GradleException("Could not find apkanalyzer executable")
        }

        val pairFounder = DebugReleasePairFounder({ debug, release ->
            val capitalizedFlavorName = release.flavorName.capitalize()
            target.tasks.create(
                "calculate${capitalizedFlavorName}ShrunkSize",
                CalculateShrunkSizeTask::class.java
            ) { task ->
                task.apkAnalyzerFile = apkAnalyserProvider
                task.reportFile = File(target.buildDir, "reports/shrinkometer/shrinkometer$capitalizedFlavorName.html")

                task.debugApkFile = debug.outputs.single().outputFile
                task.releaseApkFile = release.outputs.single().outputFile
                task.mappingFile = if (release.buildType.isMinifyEnabled) release.mappingFileProvider.get().singleFile else null

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
