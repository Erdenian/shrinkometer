package ru.erdenian.shrinkometer.core

import java.io.Writer
import java.lang.Long.signum
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.abs
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

internal fun Writer.appendStructureHtml(root: PackageNode) = appendHTML(prettyPrint = false).html {
    head {
        title("shrinkometer report")
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

@Suppress("MagicNumber")
internal fun humanReadableSize(bytes: Long): String? {
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
    return String.format(Locale.US, "%.1f %ciB", value / 1024.0, ci.current())
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

private fun readResource(path: String) = {}::class.java.getResource(path).readText()
