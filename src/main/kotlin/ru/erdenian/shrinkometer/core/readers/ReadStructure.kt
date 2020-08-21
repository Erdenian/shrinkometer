package ru.erdenian.shrinkometer.core.readers

import java.io.Reader
import java.util.LinkedList
import ru.erdenian.shrinkometer.core.ClassNode
import ru.erdenian.shrinkometer.core.FieldNode
import ru.erdenian.shrinkometer.core.MethodNode
import ru.erdenian.shrinkometer.core.PackageNode

private const val ROOT_PACKAGE_NAME = "<TOTAL>"

internal fun Reader.readStructure(): PackageNode {
    val packagesStack = LinkedList<PackageNode>()
    lateinit var currentClassNode: ClassNode

    forEachLine { line ->
        val items = line.split('\t')

        val size = items[2].toLong()
        val data = items[3]

        when (val type = items.first().first()) {
            'P' -> {
                packagesStack.popPackages(data)
                val node = readPackage(data, size)

                if (packagesStack.isNotEmpty()) {
                    packagesStack.peek().subpackages += node
                } else check(data == ROOT_PACKAGE_NAME) {
                    "Root package name is not '$ROOT_PACKAGE_NAME'. Possible report structure change."
                }
                packagesStack.push(node)
            }
            'C' -> {
                packagesStack.popPackages(data)
                val node = readClass(data, size)

                packagesStack.peek().run {
                    check(name == node.packageName) {
                        "Class package name don't match current package name"
                    }
                    classes += node
                }
                currentClassNode = node
            }
            'M' -> {
                currentClassNode.methods += readMethod(data, size).apply {
                    check(packageName == currentClassNode.packageName) {
                        "Method package name don't match class package name"
                    }
                    check(className == currentClassNode.name) {
                        "Method class name don't match parent class name"
                    }
                }
            }
            'F' -> {
                currentClassNode.fields += readField(data, size).apply {
                    check(packageName == currentClassNode.packageName) {
                        "Field package name don't match class package name"
                    }
                    check(className == currentClassNode.name) {
                        "Field class name don't match parent class name"
                    }
                }
            }
            else -> throw IllegalStateException("Unknown type: $type")
        }
    }
    return packagesStack.last.apply { name = ROOT_PACKAGE_NAME }
}

private fun LinkedList<PackageNode>.popPackages(data: String) {
    if (isEmpty()) return

    fun isInPackage(packageName: String): Boolean {
        val startsWith = data.startsWith(packageName)
        val nextIsDot = (data.getOrNull(packageName.length) == '.')
        return startsWith && (packageName.isEmpty() || nextIsDot)
    }

    var packageName = peek().name
    while (!isInPackage(packageName)) {
        pop()
        packageName = peek().name
    }
}

private fun readPackage(data: String, size: Long) = PackageNode(data.takeIf { it != ROOT_PACKAGE_NAME } ?: "", size)
private fun readClass(data: String, size: Long) = ClassNode(data, size)

private fun readMethod(data: String, size: Long): MethodNode {
    val fullClassName: String
    val returnType: String?
    val signature: String
    val methodItems = data.split(' ')
    when (methodItems.size) {
        3 -> {
            fullClassName = methodItems[0]
            returnType = methodItems[1]
            signature = methodItems[2]
        }
        2 -> {
            fullClassName = methodItems[0]
            returnType = null
            signature = methodItems[1]
        }
        else -> throw IllegalStateException("Can't parse method info: $data")
    }

    return MethodNode(fullClassName, returnType, signature, size)
}

private fun readField(data: String, size: Long): FieldNode {
    val fullClassName = data.takeWhile { it != ' ' }
    val (fieldType, name) = data.drop(fullClassName.length + 1).split(' ')
    return FieldNode(fullClassName, fieldType, name, size)
}
