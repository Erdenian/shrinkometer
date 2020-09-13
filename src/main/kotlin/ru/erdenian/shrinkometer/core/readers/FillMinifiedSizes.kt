package ru.erdenian.shrinkometer.core.readers

import ru.erdenian.shrinkometer.core.ClassNode
import ru.erdenian.shrinkometer.core.FieldNode
import ru.erdenian.shrinkometer.core.MethodNode
import ru.erdenian.shrinkometer.core.PackageNode

internal fun PackageNode.fillMinifiedSizes(release: PackageNode?) {
    minifiedSize = release?.originalSize ?: 0L

    classes.fillMinifiedSizes(release?.classes ?: mutableListOf())
    release?.classes?.forEach { log("Added class: %s", it.fullName) }

    val releaseSubpackages = release?.subpackages
    subpackages.forEach { debugPackage ->
        val releasePackageIndex = releaseSubpackages?.indexOfFirst { it.name == debugPackage.name } ?: -1
        val releasePackage = if (releasePackageIndex != -1) releaseSubpackages?.removeAt(releasePackageIndex) else null
        debugPackage.fillMinifiedSizes(releasePackage)
    }
    releaseSubpackages?.forEach { log("Added package: %s", it.fullName) }
}

@JvmName("fillClassesMinifiedSizes")
private fun List<ClassNode>.fillMinifiedSizes(releaseClasses: MutableList<ClassNode>) = forEach { debugClass ->
    val releaseClassIndex = releaseClasses.indexOfFirst { it.name == debugClass.name }
    val releaseClass = if (releaseClassIndex != -1) releaseClasses.removeAt(releaseClassIndex) else null

    debugClass.minifiedSize = releaseClass?.originalSize ?: 0L

    debugClass.fields.fillMinifiedSizes(releaseClass?.fields ?: mutableListOf())
    releaseClass?.fields?.forEach { field ->
        log("Added field: %s %s %s", releaseClass.fullName, field.type, field.name)
    }

    debugClass.methods.fillMinifiedSizes(releaseClass?.methods ?: mutableListOf())
    releaseClass?.methods?.forEach { method ->
        log("Added method %s %s", releaseClass.fullName, method.returnType?.let { "$it " } + method.signature)
    }
}

@JvmName("fillFieldsMinifiedSizes")
private fun List<FieldNode>.fillMinifiedSizes(releaseFields: MutableList<FieldNode>) = forEach { debugField ->
    val releaseField = releaseFields.filter { it.name == debugField.name }.takeIf { it.isNotEmpty() }?.single()

    if (releaseField == null) {
        debugField.minifiedSize = 0L
    } else {
        if (debugField.type != releaseField.type) log(
            "%s.%s field type changed: %s -> %s",
            debugField.fullClassName,
            debugField.name,
            debugField.type,
            releaseField.type
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
        if (debugMethod.returnType != releaseMethod.returnType) log(
            "%s.%s method return type changed: %s -> %s",
            debugMethod.fullClassName,
            debugMethod.signature,
            debugMethod.returnType,
            releaseMethod.returnType
        )

        releaseMethods.remove(releaseMethod)
        debugMethod.minifiedSize = releaseMethod.originalSize
    }
}

private fun log(format: String, vararg arguments: Any?) = println(format.format(*arguments))
