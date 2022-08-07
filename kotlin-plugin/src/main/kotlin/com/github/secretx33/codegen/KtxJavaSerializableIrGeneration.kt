package com.github.secretx33.codegen

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class KtxJavaSerializableIrGeneration(private val messageCollector: MessageCollector) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val elementTransformer = KtxJavaSerializableIrElementTransformer(messageCollector, pluginContext)
        moduleFragment.transform(elementTransformer, null)
    }
}
