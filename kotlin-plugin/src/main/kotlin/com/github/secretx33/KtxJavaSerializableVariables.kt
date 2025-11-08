package com.github.secretx33

import com.github.secretx33.util.enumSetOf
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.io.Serializable

val VALID_CLASS_KINDS = enumSetOf(ClassKind.CLASS, ClassKind.OBJECT)

val SERIALIZABLE_FQ_CLASS_NAME = FqName(Serializable::class.java.canonicalName)

val SERIALIZABLE_CLASS_ID = ClassId.topLevel(SERIALIZABLE_FQ_CLASS_NAME)

val SERIAL_VERSION_UID_NAME = Name.identifier("serialVersionUID")

val COMPANION_CLASS_NAME = Name.identifier("Companion")

val SERIALIZABLE_FQ_PROPERTY_NAME = FqName(SERIAL_VERSION_UID_NAME.identifier)
