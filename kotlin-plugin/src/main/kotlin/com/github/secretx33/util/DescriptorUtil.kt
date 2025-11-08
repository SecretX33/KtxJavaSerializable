package com.github.secretx33.util

import com.github.secretx33.SERIALIZABLE_FQ_CLASS_NAME
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.load.java.lazy.descriptors.LazyJavaClassDescriptor
import org.jetbrains.kotlin.load.java.lazy.descriptors.isJavaField
import org.jetbrains.kotlin.load.java.structure.impl.JavaClassImpl
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.model.SimpleTypeMarker
import org.jetbrains.kotlin.types.typeUtil.isBoolean

fun ClassDescriptor.getJavaFields(): List<PropertyDescriptor> {
    val variableNames = getJavaClass()?.fields?.map { it.name } ?: emptyList()
    return variableNames
        .mapNotNull { this.unsubstitutedMemberScope.getContributedVariables(it, NoLookupLocation.FROM_SYNTHETIC_SCOPE).singleOrNull() }
        .filter { it.isJavaField }
}

fun ClassDescriptor.getJavaClass(): JavaClassImpl? = (this as? LazyJavaClassDescriptor)?.jClass as? JavaClassImpl

fun ClassDescriptor.isSerializable(): Boolean =
    getSuperInterfaces().any { it.fqNameSafe == SERIALIZABLE_FQ_CLASS_NAME || it.isSerializable() }
        || getSuperClassNotAny()?.isSerializable() == true

fun IrClass.isSerializable(): Boolean = superTypes.any { it.classFqName == SERIALIZABLE_FQ_CLASS_NAME }

fun KotlinType.isPrimitiveBoolean(): Boolean = this is SimpleTypeMarker && isBoolean()
