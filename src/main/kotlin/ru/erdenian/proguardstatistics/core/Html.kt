package ru.erdenian.proguardstatistics.core

import kotlinx.html.body
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.stream.appendHTML
import kotlinx.html.ul
import java.io.Writer
import java.lang.Long.signum
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.math.abs

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

fun Writer.appendStructureHtml(root: PackageNode) = appendHTML(prettyPrint = false).html {
    head { link("result.css", "stylesheet", "text/css") }
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
                                        classNode.fields.forEach { fieldNode ->
                                            li { text(fieldNode.stringify()) }
                                        }
                                        classNode.methods.forEach { methodNode ->
                                            li { text(methodNode.stringify()) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                createRecursive(root)
                hr()
            }
        }
    }
}
