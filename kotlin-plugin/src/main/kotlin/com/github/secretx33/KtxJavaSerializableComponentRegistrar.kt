package com.github.secretx33

import com.github.secretx33.codegen.KtxJavaSerializableIrGeneration
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class KtxJavaSerializableComponentRegistrar(private val isEnabled: String) : ComponentRegistrar {

    @Suppress("unused") // Used by service loader
    constructor() : this(isEnabled = "true")

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration,
    ) {
        if (configuration[KEY_ENABLED] == false) {
            return
        }
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

//        ExpressionCodegenExtension.registerExtension(project, KtxJavaSerializableExpressionCodegenExtension())
//        ClassBuilderInterceptorExtension.registerExtension(project, KtxJavaSerializableClassGenerationInterceptor())
        IrGenerationExtension.registerExtension(project, KtxJavaSerializableIrGeneration(messageCollector))
    }
}
