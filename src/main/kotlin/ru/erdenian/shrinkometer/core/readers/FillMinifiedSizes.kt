package ru.erdenian.shrinkometer.core.readers

import ru.erdenian.shrinkometer.core.ClassNode
import ru.erdenian.shrinkometer.core.FieldNode
import ru.erdenian.shrinkometer.core.MethodNode
import ru.erdenian.shrinkometer.core.PackageNode

internal fun PackageNode.fillMinifiedSizes(release: PackageNode?) {
    if (release != null) check(this == release) {
        "Packages are not equal"
    }
    minifiedSize = release?.originalSize ?: 0L

    classes.fillMinifiedSizes(release?.classes ?: mutableListOf())
    release?.classes?.forEach { log("New class added: %s", it.fullName) }

    val releaseSubpackages = release?.subpackages
    subpackages.forEach { debugPackage ->
        val releasePackageIndex = releaseSubpackages?.indexOfFirst { it.name == debugPackage.name } ?: -1
        val releasePackage = if (releasePackageIndex != -1) releaseSubpackages?.removeAt(releasePackageIndex) else null
        debugPackage.fillMinifiedSizes(releasePackage)
    }
    releaseSubpackages?.forEach { log("New package added: %s", it.name) }
}

@JvmName("fillClassesMinifiedSizes")
private fun List<ClassNode>.fillMinifiedSizes(releaseClasses: MutableList<ClassNode>) = forEach { debugClass ->
    val releaseClassIndex = releaseClasses.indexOfFirst { it.name == debugClass.name }
    val releaseClass = if (releaseClassIndex != -1) releaseClasses.removeAt(releaseClassIndex) else null

    if (releaseClass != null) check(debugClass == releaseClass) {
        "Classes are not equal"
    }

    debugClass.minifiedSize = releaseClass?.originalSize ?: 0L

    debugClass.fields.fillMinifiedSizes(releaseClass?.fields ?: mutableListOf())
    releaseClass?.fields?.forEach { log("New field added to class %s: %s", releaseClass.fullName, it.name) }

    debugClass.methods.fillMinifiedSizes(releaseClass?.methods ?: mutableListOf())
    releaseClass?.methods?.forEach { log("New method added to class %s: %s", releaseClass.fullName, it.name) }
}

@JvmName("fillFieldsMinifiedSizes")
private fun List<FieldNode>.fillMinifiedSizes(releaseFields: MutableList<FieldNode>) = forEach { debugField ->
    val releaseField = releaseFields.filter { it.name == debugField.name }.takeIf { it.isNotEmpty() }?.single()

    if (releaseField == null) {
        debugField.minifiedSize = 0L
    } else {
        check(debugField == releaseField.copy(type = debugField.type)) {
            "Fields are not equal"
        }
        if (debugField.type != releaseField.type) log(
            "%s.%s field type changed: %s -> %s",
            debugField.fullClassName, debugField.name,
            debugField.type, releaseField.type
        )

        releaseFields.remove(releaseField)
        debugField.minifiedSize = releaseField.originalSize
    }
}

@JvmName("fillMethodsMinifiedSizes")
private fun List<MethodNode>.fillMinifiedSizes(releaseMethods: MutableList<MethodNode>) = forEach { debugMethod ->
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
        if (debugMethod.returnType != releaseMethod.returnType) log(
            "%s.%s method return type changed: %s -> %s",
            debugMethod.fullClassName, debugMethod.signature,
            debugMethod.returnType, releaseMethod.returnType
        )

        releaseMethods.remove(releaseMethod)
        debugMethod.minifiedSize = releaseMethod.originalSize
    }
}

private fun log(format: String, vararg arguments: Any?) = println(format.format(*arguments))
