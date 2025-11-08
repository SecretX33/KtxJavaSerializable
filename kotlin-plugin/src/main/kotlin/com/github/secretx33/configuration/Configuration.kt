package com.github.secretx33.configuration

import com.github.secretx33.KtxJavaSerializableConfiguration
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ConfigKey {
    val ENABLED = CompilerConfigurationKey<Boolean>(KtxJavaSerializableConfiguration.ENABLED_OPTION)
    val DEFAULT_SERIAL_VERSION_UID = CompilerConfigurationKey<Long>(KtxJavaSerializableConfiguration.DEFAULT_SERIAL_VERSION_UID_OPTION)
}

fun CompilerConfiguration.parseConfiguration(): KtxJavaSerializableConfiguration = KtxJavaSerializableConfiguration().apply {
    enabled = get(ConfigKey.ENABLED) ?: enabled
    defaultSerialVersionUID = get(ConfigKey.DEFAULT_SERIAL_VERSION_UID) ?: defaultSerialVersionUID
}
