package ru.erdenian.shrinkometer.core

import java.io.Reader
import java.util.LinkedList

fun readAndCompare(debug: Reader, release: Reader) = debug.readDebugAndCompareToReleaseSizes(release.readReleaseSizes())

@OptIn(ExperimentalStdlibApi::class)
private fun Reader.readReleaseSizes() = buildMap<String, Long> {
    forEachLine { line ->
        val byteSizeIndex = 2
        val nameIndex = 3
        val items = line.split('\t')
        put(items[nameIndex], items[byteSizeIndex].toLong())
    }
}

private fun Reader.readDebugAndCompareToReleaseSizes(release: Map<String, Long>): PackageNode {
    val packagesStack = LinkedList<PackageNode>()
    var currentClassNode = ClassNode("", "", -1, -1)
    val comparator = compareByDescending<BaseNode> { it.originalSize - it.shrankSize }

    fun popPackages(currentName: String) {
        var packageName = packagesStack.peek().name
        fun check(): Boolean {
            val startsWith = currentName.startsWith(packageName)
            val nextIsDot = (currentName.getOrNull(packageName.length) == '.')
            return startsWith && (packageName.isEmpty() || nextIsDot)
        }
        while (!check()) {
            packagesStack.pop().run {
                subpackages.sortWith(comparator)
                classes.sortWith(comparator)
            }
            packageName = packagesStack.peek().name
        }
    }

    val byteSizeIndex = 2
    val nameIndex = 3
    forEachLine { line ->
        val items = line.split('\t')

        val type = items.first().first()
        val name = items[nameIndex]
        val shrankSize = release.getOrDefault(name, 0)
        val originalSize = items[byteSizeIndex].toLong()

        when (type) {
            'P' -> {
                if (packagesStack.isEmpty()) {
                    check(name == "<TOTAL>")
                    // Name must be empty string to work properly with `startsWith` check
                    packagesStack.push(PackageNode("", shrankSize, originalSize))
                } else {
                    popPackages(name)

                    val node = PackageNode(name, shrankSize, originalSize)
                    packagesStack.peek().subpackages += node
                    packagesStack.push(node)
                }
            }
            'C' -> {
                popPackages(name)

                val packageNode = packagesStack.peek()
                val node = ClassNode(packageNode.name, name.removePrefix(packageNode.name), shrankSize, originalSize)

                packageNode.classes += node
                currentClassNode.run {
                    methods.sortWith(comparator)
                    fields.sortWith(comparator)
                }
                currentClassNode = node
            }
            'M' -> {
                val dropSize = packagesStack.peek().name.length + currentClassNode.name.length + 1
                currentClassNode.methods += MethodNode(name.drop(dropSize), shrankSize, originalSize)
            }
            'F' -> {
                val dropSize = packagesStack.peek().name.length + currentClassNode.name.length + 1
                currentClassNode.fields += FieldNode(name.drop(dropSize), shrankSize, originalSize)
            }
            else -> throw IllegalStateException("Unknown type: $type")
        }
    }
    return packagesStack.last.copy(name = "<TOTAL>")
}
