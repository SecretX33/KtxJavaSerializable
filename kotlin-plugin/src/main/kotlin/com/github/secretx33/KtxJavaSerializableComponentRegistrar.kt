package com.github.secretx33

import com.github.secretx33.configuration.parseConfiguration
import com.github.secretx33.fir.SerialFirExtensionRegistrar
import com.google.auto.service.AutoService
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
        FirExtensionRegistrarAdapter.registerExtension(SerialFirExtensionRegistrar(messageCollector, config.defaultSerialVersionUID))
    }
}
