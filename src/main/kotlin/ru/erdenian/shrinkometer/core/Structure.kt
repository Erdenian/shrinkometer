@file:Suppress("DataClassPrivateConstructor")

package ru.erdenian.shrinkometer.core

import kotlin.properties.Delegates

abstract class BaseNode {

    abstract val packageName: String
    abstract val name: String

    var originalSize by Delegates.notNull<Long>()
        internal set

    var minifiedSize by Delegates.notNull<Long>()
        internal set
}

@Suppress("DataClassShouldBeImmutable")
data class PackageNode private constructor(override var name: String) : BaseNode() {

    override val packageName = name.take(maxOf(name.lastIndexOf('.'), 0))

    constructor(name: String, originalSize: Long) : this(name) {
        this.originalSize = originalSize
    }

    val subpackages: MutableList<PackageNode> = mutableListOf()
    val classes: MutableList<ClassNode> = mutableListOf()
}

data class ClassNode(val fullName: String) : BaseNode() {

    override val name = fullName.takeLastWhile { it != '.' }
    override val packageName = fullName.dropLast(name.length + 1)

    constructor(fullName: String, originalSize: Long) : this(fullName) {
        this.originalSize = originalSize
    }

    val methods: MutableList<MethodNode> = mutableListOf()
    val fields: MutableList<FieldNode> = mutableListOf()
}

data class MethodNode private constructor(
    val fullClassName: String,
    val returnType: String?,
    val signature: String
) : BaseNode() {

    val className = fullClassName.takeLastWhile { it != '.' }
    override val packageName = fullClassName.dropLast(className.length + 1)

    override val name = signature.takeWhile { it != '(' }
    val argumentTypes = signature.substring(name.length + 1, signature.length - 1).split(' ')

    constructor(
        fullClassName: String,
        returnType: String?,
        signature: String,
        originalSize: Long
    ) : this(fullClassName, returnType, signature) {
        this.originalSize = originalSize
    }
}

data class FieldNode private constructor(
    val fullClassName: String,
    val type: String,
    override val name: String
) : BaseNode() {

    val className = fullClassName.takeLastWhile { it != '.' }
    override val packageName = fullClassName.dropLast(className.length + 1)

    constructor(fullClassName: String, type: String, name: String, originalSize: Long) : this(fullClassName, type, name) {
        this.originalSize = originalSize
    }
}
