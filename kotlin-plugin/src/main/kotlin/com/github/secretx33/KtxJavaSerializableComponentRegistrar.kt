package com.github.secretx33

import com.github.secretx33.configuration.parseConfiguration
import com.github.secretx33.fir.SerialFirExtensionRegistrar
import com.github.secretx33.ir.SerialIrExtensionRegistrar
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@AutoService(CompilerPluginRegistrar::class)
class KtxJavaSerializableComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(
        configuration: CompilerConfiguration,
    ) {
        val config = configuration.parseConfiguration()
        if (!config.enabled) return

        val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        // Register FIR extension (frontend - declares structure)
        FirExtensionRegistrarAdapter.registerExtension(SerialFirExtensionRegistrar(messageCollector))

        // Register IR extension (backend - adds initializer)
        IrGenerationExtension.registerExtension(SerialIrExtensionRegistrar(messageCollector, config.defaultSerialVersionUID))
    }
}
