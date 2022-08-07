package com.github.secretx33.util

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

internal fun MessageCollector.verbose(message: String, location: CompilerMessageSourceLocation? = null) =
    report(CompilerMessageSeverity.LOGGING, message, location)

internal fun MessageCollector.info(message: String, location: CompilerMessageSourceLocation? = null) =
    report(CompilerMessageSeverity.INFO, message, location)

internal fun MessageCollector.warn(message: String, location: CompilerMessageSourceLocation? = null) =
    report(CompilerMessageSeverity.WARNING, message, location)

internal fun MessageCollector.strongWarn(message: String, location: CompilerMessageSourceLocation? = null) =
    report(CompilerMessageSeverity.STRONG_WARNING, message, location)

internal fun MessageCollector.error(message: String, location: CompilerMessageSourceLocation? = null) =
    report(CompilerMessageSeverity.EXCEPTION, message, location)
