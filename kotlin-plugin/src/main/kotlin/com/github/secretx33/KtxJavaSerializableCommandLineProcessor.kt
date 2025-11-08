package com.github.secretx33

import com.github.secretx33.KtxJavaSerializableConfiguration.Companion.DEFAULT_SERIAL_VERSION_UID_OPTION
import com.github.secretx33.KtxJavaSerializableConfiguration.Companion.ENABLED_OPTION
import com.github.secretx33.configuration.ConfigKey
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class KtxJavaSerializableCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "ktxserializablejava"

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = ENABLED_OPTION,
            valueDescription = "<true|false>",
            description = "Whether plugin is enabled",
            required = false,
        ),
        CliOption(
            optionName = DEFAULT_SERIAL_VERSION_UID_OPTION,
            valueDescription = "<number>",
            description = "The default serialVersionUID to use when none is specified; defaults to ",
            required = false,
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) = when (option.optionName) {
        ENABLED_OPTION -> configuration.put(
            ConfigKey.ENABLED,
            value.toBooleanStrictOrNull() ?: error("Invalid value for $ENABLED_OPTION: $value")
        )

        DEFAULT_SERIAL_VERSION_UID_OPTION -> configuration.put(
            ConfigKey.DEFAULT_SERIAL_VERSION_UID,
            value.toLongOrNull() ?: error("Invalid value for $DEFAULT_SERIAL_VERSION_UID_OPTION: $value")
        )

        else -> throw IllegalArgumentException("Unexpected config option: ${option.optionName}")
    }
}
