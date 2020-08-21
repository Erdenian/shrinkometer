package ru.erdenian.shrinkometer.core

import java.io.Reader

internal fun readAndCompare(debug: Reader, release: Reader) = debug.readStructure().apply {
    fillMinifiedSizes(release.readStructure())
}

private fun PackageNode.fillMinifiedSizes(release: PackageNode?) {
    if (release != null) check(this == release) {
        "Packages are not equal"
    }
    minifiedSize = release?.originalSize ?: 0L

    classes.forEach { debugClass ->
        val releaseClass = release?.classes?.find { it.name == debugClass.name }

        if (releaseClass == null) {
            debugClass.minifiedSize = 0L
            debugClass.fields.forEach { it.minifiedSize = 0L }
            debugClass.methods.forEach { it.minifiedSize = 0L }
        } else {
            check(debugClass == releaseClass) {
                "Classes are not equal"
            }
            debugClass.minifiedSize = releaseClass.originalSize

            run {
                val releaseFields = releaseClass.fields
                debugClass.fields.forEach { debugField ->
                    val releaseField = releaseFields.filter { it.name == debugField.name }.takeIf { it.isNotEmpty() }?.single()
                    if (releaseField == null) {
                        debugField.minifiedSize = 0L
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
                        debugField.minifiedSize = releaseField.originalSize
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
                        debugMethod.minifiedSize = 0L
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
                        debugMethod.minifiedSize = releaseMethod.originalSize
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
            debugPackage.minifiedSize = 0
            debugPackage.classes.forEach { debugClass ->
                debugClass.minifiedSize = 0L
                debugClass.fields.forEach { it.minifiedSize = 0L }
                debugClass.methods.forEach { it.minifiedSize = 0L }
            }
            debugPackage.fillMinifiedSizes(null)
        } else {
            debugPackage.fillMinifiedSizes(releaseSubpackages?.removeAt(releasePackageIndex))
        }
    }
    releaseSubpackages?.forEach { subpackage ->
        "New package added: %s"
            .format(subpackage.name)
            .let(::println)
    }
}
