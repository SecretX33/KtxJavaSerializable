package com.github.secretx33.util

import com.github.secretx33.configuration.SERIALIZABLE_FQ_CLASS_NAME
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

fun ClassDescriptor.isSerializable(): Boolean =
    getSuperInterfaces().any { it.fqNameSafe == SERIALIZABLE_FQ_CLASS_NAME || it.isSerializable() }
        || getSuperClassNotAny()?.isSerializable() == true

fun IrClass.isSerializable(): Boolean = superTypes.any { it.classFqName == SERIALIZABLE_FQ_CLASS_NAME }
