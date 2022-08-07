package com.github.secretx33

import com.github.secretx33.KtxJavaSerializableCommandLineProcessor.Companion.ENABLED_OPTION
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val KEY_ENABLED = CompilerConfigurationKey<Boolean>(ENABLED_OPTION)

@AutoService(CommandLineProcessor::class)
class KtxJavaSerializableCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "ktxserializablejava"

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = ENABLED_OPTION,
            valueDescription = "<true|false>",
            description = "whether plugin is enabled",
            required = false,
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) = when (option.optionName) {
        ENABLED_OPTION -> configuration.put(KEY_ENABLED, value.toBooleanStrictOrNull() ?: error("Invalid value for $ENABLED_OPTION: $value"))
        else -> throw IllegalArgumentException("Unexpected config option: ${option.optionName}")
    }

    internal companion object {
        const val ENABLED_OPTION = "enabled"
    }
}
