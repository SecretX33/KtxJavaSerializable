package com.github.secretx33

import com.github.secretx33.util.enumSetOf
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.org.objectweb.asm.Opcodes
import java.io.Serializable

const val SERIALIZABLE_PROPERTY_NAME = "serialVersionUID"
val SERIALIZABLE_FQ_PROPERTY_NAME = FqName(SERIALIZABLE_PROPERTY_NAME)
val SERIALIZABLE_FQ_CLASS_NAME = FqName(Serializable::class.java.canonicalName)
val VALID_CLASS_KINDS = enumSetOf(ClassKind.CLASS, ClassKind.OBJECT)
val SERIALIZABLE_OPTCODES = setOf(Opcodes.ACC_PRIVATE, Opcodes.ACC_STATIC, Opcodes.ACC_FINAL, Opcodes.ACC_TRANSIENT, Opcodes.ACC_SYNTHETIC).reduce { acc, i -> acc or i }
const val SERIALIZABLE_DEFAULT_VALUE = 1L
object SerializableOrigin : IrDeclarationOriginImpl("SYNTHETIC_GENERATED_SERIALIZABLE_FIELD", isSynthetic = true)
