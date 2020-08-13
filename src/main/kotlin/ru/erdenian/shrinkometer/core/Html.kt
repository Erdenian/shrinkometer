package ru.erdenian.shrinkometer.core

import kotlinx.html.body
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe
import java.io.Writer
import java.lang.Long.signum
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.time.ZonedDateTime
import kotlin.math.abs

fun Writer.appendStructureHtml(root: PackageNode) = appendHTML(prettyPrint = false).html {
    head {
        title("ProGuardStatistics report")
        style { unsafe { +readResource("/styles.css") } }
    }
    body {
        div("css-treeview") {
            ul {
                fun createRecursive(packageNode: PackageNode) {
                    li {
                        checkBoxInput { id = packageNode.name }
                        label {
                            htmlFor = packageNode.name
                            text(packageNode.stringify())
                        }

                        ul {
                            packageNode.subpackages.forEach { createRecursive(it) }

                            packageNode.classes.forEach { classNode ->
                                li {
                                    checkBoxInput { id = classNode.packageName + classNode.name }
                                    label {
                                        htmlFor = classNode.packageName + classNode.name
                                        text(classNode.stringify())
                                    }

                                    ul {
                                        classNode.fields.forEach { li { text(it.stringify()) } }
                                        classNode.methods.forEach { li { text(it.stringify()) } }
                                    }
                                }
                            }
                        }
                    }
                }
                createRecursive(root)

                hr()
                +"Generated at ${ZonedDateTime.now()}"
            }
        }
    }
}

private fun humanReadableByteCountBin(bytes: Long): String? {
    val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else abs(bytes)
    if (absB < 1024) return "$bytes B"
    var value = absB
    val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
    var i = 40
    while ((i >= 0) && (absB > 0xfffccccccccccccL shr i)) {
        value = value shr 10
        ci.next()
        i -= 10
    }
    value *= signum(bytes).toLong()
    return String.format("%.1f %ciB", value / 1024.0, ci.current())
}

private fun BaseNode.stringify() = String.format(
    "%s - %s (%.0f%%) from %s to %s",
    name,
    humanReadableByteCountBin(originalSize.toLong() - shrankSize),
    (1.0f - shrankSize.toFloat() / originalSize) * 100.0f,
    humanReadableByteCountBin(originalSize.toLong()),
    humanReadableByteCountBin(shrankSize.toLong())
)

private fun readResource(path: String) = {}::class.java.getResource(path).readText()