package ru.erdenian.shrinkometer.gradle

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import ru.erdenian.shrinkometer.core.humanReadableSize
import ru.erdenian.shrinkometer.core.readAndCompare
import ru.erdenian.shrinkometer.core.reports.generateHtmlReport

@Suppress("LateinitUsage")
open class ShrinkometerTask : DefaultTask() {

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
    internal fun taskAction() {
        fun execAnalyzer(apk: File, params: String) =
            Runtime.getRuntime().exec("${apkAnalyzerFile.get()} dex packages $apk $params").inputStream.reader()

        val result = readAndCompare(
            execAnalyzer(debugApkFile, "--defined-only"),
            execAnalyzer(releaseApkFile, "--defined-only ${mappingFile?.let { "--proguard-mappings $it" } ?: ""}")
        )
        logger.quiet(
            "Classes size reduced from {} to {}",
            humanReadableSize(result.originalSize),
            humanReadableSize(result.minifiedSize)
        )

        result.generateHtmlReport(reportFile)
        logger.quiet("Successfully generated HTML report at $reportFile")
    }
}
