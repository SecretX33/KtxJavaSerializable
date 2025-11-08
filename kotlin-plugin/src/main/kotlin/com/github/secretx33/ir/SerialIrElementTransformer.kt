package com.github.secretx33.ir

import com.github.secretx33.configuration.SERIALIZABLE_FQ_PROPERTY_NAME
import com.github.secretx33.util.error
import com.github.secretx33.util.info
import com.github.secretx33.util.isSerializable
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.util.DumpIrTreeOptions
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.toIrConst

class SerialIrElementTransformer(
    private val messageCollector: MessageCollector,
    private val context: IrPluginContext,
    private val defaultSerialVersionUID: Long,
) : IrElementTransformerVoidWithContext() {

    override fun visitClassNew(declaration: IrClass): IrClass {
        val irClass = super.visitClassNew(declaration) as? IrClass
            ?: error("Unexpected object was returned from IrElementTransformerVoidWithContext.visitClassNew")

        messageCollector.info("${"-".repeat(100)}\nName: ${irClass.name}. Is companion: ${irClass.isCompanion}. Is Serializable: ${irClass.isSerializable()}. Dump: \n${irClass.dump(DumpIrTreeOptions(stableOrder = true))}")

        // Only process Serializable main classes (not companion objects themselves)
        if (irClass.isCompanion || !irClass.isSerializable()) return irClass

        val companionObject = irClass.findCompanionObject() ?: run {
            messageCollector.error("No companion object found for Serializable class ${irClass.name}, but this should never happen as companion object is generated during FIR phase - skipping")
            return irClass
        }
        messageCollector.info("Found companion object: ${companionObject.name}")

        val serialVersionUIDProperty = companionObject.findGeneratedSerialVersionUIDField() ?: run {
            // This should never happen as this field is generated during FIR phase
            messageCollector.error("serialVersionUID property not found in companion of ${irClass.name}, but this should never happen as companion object is generated during FIR phase - skipping")
            return irClass
        }
        messageCollector.info("Found serialVersionUID property: ${serialVersionUIDProperty.name}")

        val backingField = serialVersionUIDProperty.backingField ?: run {
            messageCollector.error("No backing field for serialVersionUID in ${irClass.name}, but this should never happen as it should have been generated during FIR phase - skipping")
            return irClass
        }
        if (backingField.initializer != null) {
            messageCollector.info("serialVersionUID in ${irClass.name} already has an initializer - skipping")
            return irClass
        }

        backingField.initializer = createSerialVersionUIDInitializer(irClass)
        messageCollector.info("Successfully added initializer (value: $defaultSerialVersionUID) to serialVersionUID")
        messageCollector.info("${"-".repeat(100)}\n[MODIFIED!!] Name: ${irClass.name}. Dump: \n${irClass.dump(DumpIrTreeOptions(stableOrder = true))}")

        return irClass
    }

    private fun IrClass.findCompanionObject(): IrClass? = declarations.firstOrNull {
        it is IrClass && it.isCompanion
    } as? IrClass

    private fun IrClass.findGeneratedSerialVersionUIDField(): IrProperty? = declarations.firstOrNull {
        it is IrProperty
            && it.name.asString() == SERIALIZABLE_FQ_PROPERTY_NAME.shortName().asString()
            && it.backingField?.type?.isLong() == true
    } as? IrProperty

    private fun createSerialVersionUIDInitializer(irClass: IrClass): IrExpressionBody =
        irClass.factory.createExpressionBody(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            expression = defaultSerialVersionUID.toIrConst(context.irBuiltIns.longType)
        )
}
