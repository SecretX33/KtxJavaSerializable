package com.github.secretx33.codegen

import com.github.secretx33.SERIALIZABLE_PROPERTY_NAME
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class SerialVersionMethodVisitor(
    private val clazz: ClassDescriptor,
    original: MethodVisitor,
) : MethodVisitor(Opcodes.ASM5, original) {

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.RETURN) {
            InstructionAdapter(this).apply {
                load(Opcodes.LCONST_1, Type.LONG_TYPE)
                putstatic(clazz.fqNameSafe.asString(), SERIALIZABLE_PROPERTY_NAME, Type.LONG_TYPE.descriptor)
            }
        }
        super.visitInsn(opcode)
    }
}
