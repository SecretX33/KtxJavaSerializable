package com.github.secretx33.ir

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class SerialIrExtensionRegistrar(
    private val messageCollector: MessageCollector,
    private val defaultSerialVersionUID: Long,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val elementTransformer = SerialIrElementTransformer(
            messageCollector = messageCollector,
            context = pluginContext,
            defaultSerialVersionUID = defaultSerialVersionUID,
        )
        moduleFragment.transform(elementTransformer, null)
    }
}
