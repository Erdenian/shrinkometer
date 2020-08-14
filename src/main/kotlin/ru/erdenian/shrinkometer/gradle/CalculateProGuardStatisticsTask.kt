package ru.erdenian.shrinkometer.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.InputChanges
import ru.erdenian.shrinkometer.core.appendStructureHtml
import ru.erdenian.shrinkometer.core.readAndCompare
import java.io.File
import java.io.FileWriter

@Suppress("LateinitUsage")
open class CalculateProGuardStatisticsTask : DefaultTask() {

    @get:InputFile
    lateinit var apkAnalyzerFile: Provider<File>

    @get:InputFile
    lateinit var debugApkFile: File

    @get:InputFile
    lateinit var releaseApkFile: File

    @get:InputFile
    @get:Optional
    var mappingFile: File? = null

    @get:OutputFile
    lateinit var reportFile: File

    @TaskAction
    internal fun taskAction(inputs: InputChanges) {
        fun execAnalyzer(apk: File, params: String) =
            Runtime.getRuntime().exec("${apkAnalyzerFile.get()} dex packages $apk $params").inputStream.reader()

        val result = readAndCompare(
            execAnalyzer(debugApkFile, "--defined-only"),
            execAnalyzer(releaseApkFile, "--defined-only ${mappingFile?.let { "--proguard-mappings $it" } ?: ""}")
        )

        FileWriter(reportFile).use { it.appendStructureHtml(result) }
    }
}
