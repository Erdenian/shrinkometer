package ru.erdenian.shrinkometer.core

interface BaseNode {
    val name: String
    val shrankSize: Long
    val originalSize: Long
}

data class PackageNode(
    override val name: String,
    override val shrankSize: Long,
    override val originalSize: Long,
    val subpackages: MutableList<PackageNode> = mutableListOf(),
    val classes: MutableList<ClassNode> = mutableListOf()
) : BaseNode

data class ClassNode(
    val packageName: String,
    override val name: String,
    override val shrankSize: Long,
    override val originalSize: Long,
    val methods: MutableList<MethodNode> = mutableListOf(),
    val fields: MutableList<FieldNode> = mutableListOf()
) : BaseNode

data class MethodNode(
    override val name: String,
    override val shrankSize: Long,
    override val originalSize: Long
) : BaseNode

data class FieldNode(
    override val name: String,
    override val shrankSize: Long,
    override val originalSize: Long
) : BaseNode
