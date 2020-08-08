package ru.erdenian.proguardstatistics

import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties

@OptIn(ExperimentalStdlibApi::class)
fun main() {
    val sdkDir = Properties().run {
        load(FileInputStream("local.properties"))
        getProperty("sdk.dir")
    }.removeSuffix(File.separator)
    val apkAnalyzerPath = sdkDir + "|cmdline-tools|1.0|bin|apkanalyzer.bat".replace('|', File.separatorChar)
    val testDir = "./test"

    fun execAnalyzer(apkName: String, params: String) =
        Runtime.getRuntime().exec("$apkAnalyzerPath dex packages $testDir/$apkName $params").inputStream.reader()

    val result = readAndCompare(
        execAnalyzer("YoPayWallet-0.2.0.1-debug.apk", "--defined-only"),
        execAnalyzer("YoPayWallet-0.2.0.1-release.apk", "--defined-only --proguard-folder $testDir/mappings")
    )

    FileWriter("result.html").use { it.appendStructureHtml(result) }
}
