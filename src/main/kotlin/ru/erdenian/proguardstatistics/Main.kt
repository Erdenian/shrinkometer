package ru.erdenian.proguardstatistics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties

@OptIn(ExperimentalStdlibApi::class)
fun main() = runBlockingGlobalScope {
    val sdkDir = Properties().run {
        load(FileInputStream("local.properties"))
        getProperty("sdk.dir")
    }.removeSuffix(File.separator)
    val apkAnalyzerPath = sdkDir + "|cmdline-tools|1.0|bin|apkanalyzer.bat".replace('|', File.separatorChar)
    val testDir = "./test"

    fun Runtime.execAnalyzer(apkName: String, params: String) = exec("$apkAnalyzerPath dex packages $testDir/$apkName $params")

    val result = readAndCompare(
        Runtime.getRuntime()
            .execAnalyzer("YoPayWallet-0.2.0.1-debug.apk", "--defined-only")
            .inputStream.reader(),
        Runtime.getRuntime()
            .execAnalyzer("YoPayWallet-0.2.0.1-release.apk", "--defined-only --proguard-folder $testDir/mappings")
            .inputStream.reader()
    )

    FileWriter("result.html").run {
        appendStructureHtml(result)
        close()
    }
}

private fun runBlockingGlobalScope(block: suspend CoroutineScope.() -> Unit) = runBlocking {
    GlobalScope.launch(block = block).join()
}
