package ru.erdenian.proguardstatistics

interface BaseNode {
    val name: String
    val size: Int
    val oldSize: Int
}

data class PackageNode(
    override val name: String,
    override val size: Int,
    override val oldSize: Int,
    val subpackages: MutableList<PackageNode> = mutableListOf(),
    val classes: MutableList<ClassNode> = mutableListOf()
) : BaseNode

data class ClassNode(
    val packageName: String,
    override val name: String,
    override val size: Int,
    override val oldSize: Int,
    val methods: MutableList<MethodNode> = mutableListOf(),
    val fields: MutableList<FieldNode> = mutableListOf()
) : BaseNode

data class MethodNode(
    override val name: String,
    override val size: Int,
    override val oldSize: Int
) : BaseNode

data class FieldNode(
    override val name: String,
    override val size: Int,
    override val oldSize: Int
) : BaseNode
