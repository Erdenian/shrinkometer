package ru.erdenian.proguardstatistics

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.InputChanges
import java.io.File
import java.io.FileWriter

open class CalculateProGuardStatisticsTask : DefaultTask() {

    @get:Input
    var sdkPath: String? = null

    @get:Input
    var debugApkPath: String? = null

    @get:Input
    var releaseApkPath: String? = null

    @get:Input
    var mappingFilePath: String? = null

    @get:OutputDirectory
    var reportFilePath: String? = null

    @TaskAction
    internal fun taskAction(inputs: InputChanges) {
        val apkAnalyzerPath = sdkPath + "|cmdline-tools|1.0|bin|apkanalyzer.bat".replace('|', File.separatorChar)

        fun execAnalyzer(apkPath: String, params: String) =
            Runtime.getRuntime().exec("$apkAnalyzerPath dex packages $apkPath $params").inputStream.reader()

        val result = readAndCompare(
            execAnalyzer(debugApkPath!!, "--defined-only"),
            execAnalyzer(releaseApkPath!!, "--defined-only --proguard-mappings $mappingFilePath")
        )

        FileWriter(reportFilePath).use { it.appendStructureHtml(result) }
    }
}
