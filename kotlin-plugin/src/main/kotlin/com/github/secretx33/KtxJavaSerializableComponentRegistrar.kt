package com.github.secretx33

import com.github.secretx33.codegen.KtxJavaSerializableIrGeneration
import com.github.secretx33.fir.SerialFirExtensionRegistrar
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class KtxJavaSerializableComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(
        configuration: CompilerConfiguration,
    ) {
        if (configuration[KEY_ENABLED] == false) {
            return
        }
        val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        IrGenerationExtension.registerExtension(KtxJavaSerializableIrGeneration(messageCollector))
        FirExtensionRegistrarAdapter.registerExtension(SerialFirExtensionRegistrar(messageCollector))
    }
}
