package ru.erdenian.shrinkometer.core.reports

import java.io.File
import java.io.FileWriter
import java.time.ZonedDateTime
import java.util.Locale
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.li
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe
import ru.erdenian.shrinkometer.core.BaseNode
import ru.erdenian.shrinkometer.core.PackageNode
import ru.erdenian.shrinkometer.core.humanReadableSize
import ru.erdenian.shrinkometer.core.readResource

internal fun PackageNode.generateHtmlReport(file: File) = FileWriter(file).use { writer ->
    val comparator = compareByDescending<BaseNode> { it.originalSize - it.minifiedSize }
    fun <T : BaseNode> MutableList<T>.forEachSorted(action: (T) -> Unit) {
        sortWith(comparator)
        for (element in this) action(element)
    }

    writer.appendHTML(prettyPrint = false).html {
        head {
            title("shrinkometer report")
            style { unsafe { +readResource("/styles.css") } }
        }
        body {
            ul {
                id = "myUL"

                fun createRecursive(packageNode: PackageNode) {
                    li {
                        span("caret package") { +packageNode.stringify() }

                        ul("nested") {
                            packageNode.subpackages.forEachSorted { createRecursive(it) }

                            packageNode.classes.forEachSorted { classNode ->
                                li {
                                    span("caret class") { +classNode.stringify() }

                                    ul("nested") {
                                        classNode.fields.forEachSorted { li { span("field") { +it.stringify() } } }
                                        classNode.methods.forEachSorted { li { span("method") { +it.stringify() } } }
                                    }
                                }
                            }
                        }
                    }
                }
                createRecursive(this@generateHtmlReport)

                hr()
                +"Generated at ${ZonedDateTime.now()}"

                script { unsafe { +readResource("/script.js") } }
            }
        }
    }
}

@Suppress("MagicNumber")
private fun BaseNode.stringify() = String.format(
    Locale.US,
    "%s - %s (%.0f%%) from %s to %s",
    name,
    humanReadableSize(originalSize - minifiedSize),
    (1.0f - minifiedSize.toFloat() / originalSize) * 100.0f,
    humanReadableSize(originalSize),
    humanReadableSize(minifiedSize)
)
