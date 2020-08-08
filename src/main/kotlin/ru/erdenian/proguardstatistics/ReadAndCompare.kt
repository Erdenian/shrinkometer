package ru.erdenian.proguardstatistics

import java.io.Reader
import java.util.LinkedList

private const val DEBUG_REPORT_SIZE_MULTIPLIER = 6

fun readAndCompare(debug: Reader, release: Reader) =
    debug.readDebugAndCompareToReleaseSizes(release.readReleaseSizes()).readStructure()

@OptIn(ExperimentalStdlibApi::class)
private fun Reader.readReleaseSizes() = buildMap<String, Int> {
    forEachLine { line ->
        val byteSizeIndex = 2
        val nameIndex = 3
        val items = line.split('\t')
        put(items[nameIndex], items[byteSizeIndex].toInt())
    }
}

private fun Reader.readDebugAndCompareToReleaseSizes(release: Map<String, Int>) = LinkedHashMap<String, ItemInfo>(
    release.size * DEBUG_REPORT_SIZE_MULTIPLIER
).apply {
    forEachLine { line ->
        val byteSizeIndex = 2
        val nameIndex = 3
        val items = line.split('\t')

        val name = items[nameIndex]
        val size = items[byteSizeIndex].toInt()

        put(name, ItemInfo(items.first().first(), release.getOrDefault(name, 0), size))
    }
}

private fun LinkedHashMap<String, ItemInfo>.readStructure(): PackageNode {
    val packagesStack = LinkedList<PackageNode>()
    var currentClassNode = ClassNode("", "", -1, -1)
    val comparator = compareByDescending<BaseNode> { it.oldSize - it.size }

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

    forEach { (name, info) ->
        when (info.type) {
            'P' -> {
                if (packagesStack.isEmpty()) {
                    check(name == "<TOTAL>")
                    // Name must be empty string to work properly with `startsWith` check
                    packagesStack.push(PackageNode("", info.size, info.oldSize))
                } else {
                    popPackages(name)

                    val node = PackageNode(name, info.size, info.oldSize)
                    packagesStack.peek().subpackages += node
                    packagesStack.push(node)
                }
            }
            'C' -> {
                popPackages(name)

                val packageNode = packagesStack.peek()
                val node = ClassNode(packageNode.name, name.removePrefix(packageNode.name), info.size, info.oldSize)

                packageNode.classes += node
                currentClassNode.run {
                    methods.sortWith(comparator)
                    fields.sortWith(comparator)
                }
                currentClassNode = node
            }
            'M' -> {
                val dropSize = packagesStack.peek().name.length + currentClassNode.name.length + 1
                currentClassNode.methods += MethodNode(name.drop(dropSize), info.size, info.oldSize)
            }
            'F' -> {
                val dropSize = packagesStack.peek().name.length + currentClassNode.name.length + 1
                currentClassNode.fields += FieldNode(name.drop(dropSize), info.size, info.oldSize)
            }
            else -> throw IllegalStateException("Unknown type: ${info.type}")
        }
    }
    return packagesStack.last.copy(name = "<TOTAL>")
}

private data class ItemInfo(
    val type: Char,
    val size: Int,
    val oldSize: Int
)
