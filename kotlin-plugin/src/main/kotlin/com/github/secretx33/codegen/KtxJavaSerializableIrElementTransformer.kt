package com.github.secretx33.codegen

import com.github.secretx33.SERIALIZABLE_DEFAULT_VALUE
import com.github.secretx33.SERIALIZABLE_FQ_PROPERTY_NAME
import com.github.secretx33.SerializableOrigin
import com.github.secretx33.util.info
import com.github.secretx33.util.isSerializable
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.dump

class KtxJavaSerializableIrElementTransformer(
    private val messageCollector: MessageCollector,
    private val context: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitClassNew(declaration: IrClass): IrClass {
        val irClass = super.visitClassNew(declaration) as? IrClass
            ?: error("Unespected object was returned from IrElementTransformerVoidWithContext.visitClassNew")

        messageCollector.info("${"-".repeat(100)}\nName: ${irClass.name}. Dump: \n${irClass.dump(stableOrder = true)}")

        if (declaration.isCompanion || !declaration.isSerializable()) return irClass

        val expressionBody = irClass.factory.createExpressionBody(SERIALIZABLE_DEFAULT_VALUE.toIrConst(context.irBuiltIns.longType))
        val serializableField = irClass.factory.buildField {
            name = SERIALIZABLE_FQ_PROPERTY_NAME.shortName()
            type = context.irBuiltIns.longType
            origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
            isFinal = true
            isStatic = true
            visibility = DescriptorVisibilities.PRIVATE
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
        }.apply {
            parent = irClass
            initializer = expressionBody
        }
        irClass.addProperty {
            name = SERIALIZABLE_FQ_PROPERTY_NAME.shortName()
            origin = SerializableOrigin
            visibility = DescriptorVisibilities.PRIVATE
            isConst = true
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
        }.apply {
            parent = irClass
            backingField = serializableField
        }

        messageCollector.info("${"-".repeat(100)}\n[MODIFIED!!] Name: ${irClass.name}. Dump: \n${irClass.dump(stableOrder = true)}")

        return irClass
    }
}
