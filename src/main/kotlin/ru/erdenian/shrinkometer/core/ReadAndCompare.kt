package ru.erdenian.shrinkometer.core

import java.io.Reader
import java.util.LinkedList

internal fun readAndCompare(debug: Reader, release: Reader) = debug.readStructure().apply {
    fillShrankSizes(release.readStructure())
}

private fun PackageNode.fillShrankSizes(release: PackageNode?) {
    if (release != null) check(this == release) {
        "Packages are not equal"
    }
    shrankSize = release?.originalSize ?: 0L

    classes.forEach { debugClass ->
        val releaseClass = release?.classes?.find { it.name == debugClass.name }

        if (releaseClass == null) {
            debugClass.shrankSize = 0L
            debugClass.fields.forEach { it.shrankSize = 0L }
            debugClass.methods.forEach { it.shrankSize = 0L }
        } else {
            check(debugClass == releaseClass) {
                "Classes are not equal"
            }
            debugClass.shrankSize = releaseClass.originalSize

            run {
                val releaseFields = releaseClass.fields
                debugClass.fields.forEach { debugField ->
                    val releaseField = releaseFields.filter { it.name == debugField.name }.takeIf { it.isNotEmpty() }?.single()
                    if (releaseField == null) {
                        debugField.shrankSize = 0L
                    } else {
                        check(debugField == releaseField.copy(type = debugField.type)) {
                            "Fields are not equal"
                        }
                        if (debugField.type != releaseField.type) {
                            "%s.%s field type changed: %s -> %s"
                                .format(
                                    debugField.fullClassName, debugField.name,
                                    debugField.type, releaseField.type
                                )
                                .let(::println)
                        }

                        releaseFields.remove(releaseField)
                        debugField.shrankSize = releaseField.originalSize
                    }
                }
                releaseFields.forEach { field ->
                    "New field added to class %s: %s"
                        .format(releaseClass.fullName, field.name)
                        .let(::println)
                }
            }

            run {
                val releaseMethods = releaseClass.methods
                debugClass.methods.forEach { debugMethod ->
                    val releaseMethod = releaseMethods.filter { it.signature == debugMethod.signature }.run {
                        if (size <= 1) singleOrNull()
                        else filter { it.returnType == debugMethod.returnType }.takeIf { it.isNotEmpty() }?.single()
                    }
                    if (releaseMethod == null) {
                        debugMethod.shrankSize = 0L
                    } else {
                        check(debugMethod == releaseMethod.copy(returnType = debugMethod.returnType)) {
                            "Methods are not equal"
                        }
                        if (debugMethod.returnType != releaseMethod.returnType) {
                            "%s.%s method return type changed: %s -> %s"
                                .format(
                                    debugMethod.fullClassName, debugMethod.signature,
                                    debugMethod.returnType, releaseMethod.returnType
                                )
                                .let(::println)
                        }

                        releaseMethods.remove(releaseMethod)
                        debugMethod.shrankSize = releaseMethod.originalSize
                    }
                }
                releaseMethods.forEach { method ->
                    "New method added to class %s: %s"
                        .format(releaseClass.fullName, method.name)
                        .let(::println)
                }
            }
        }
    }

    val releaseSubpackages = release?.subpackages
    subpackages.forEach { debugPackage ->
        val releasePackageIndex = releaseSubpackages?.indexOfFirst { it.name == debugPackage.name } ?: -1
        if (releasePackageIndex == -1) {
            debugPackage.shrankSize = 0
            debugPackage.classes.forEach { debugClass ->
                debugClass.shrankSize = 0L
                debugClass.fields.forEach { it.shrankSize = 0L }
                debugClass.methods.forEach { it.shrankSize = 0L }
            }
            debugPackage.fillShrankSizes(null)
        } else {
            debugPackage.fillShrankSizes(releaseSubpackages?.removeAt(releasePackageIndex))
        }
    }
    releaseSubpackages?.forEach { subpackage ->
        "New package added: %s"
            .format(subpackage.name)
            .let(::println)
    }
}

private const val ROOT_PACKAGE_NAME = "<TOTAL>"

private fun Reader.readStructure(): PackageNode {
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
