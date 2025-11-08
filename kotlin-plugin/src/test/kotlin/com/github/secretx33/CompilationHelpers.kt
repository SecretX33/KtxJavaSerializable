package com.github.secretx33

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.lang.reflect.Field

/**
 * Compiles the given source file with the KtxJavaSerializable compiler plugin enabled.
 *
 * @param sourceFile The Kotlin source file to compile
 * @param plugin The compiler plugin registrar to use (defaults to KtxJavaSerializableComponentRegistrar)
 * @return The compilation result
 */
@OptIn(ExperimentalCompilerApi::class)
fun compile(
    sourceFile: SourceFile,
    plugin: CompilerPluginRegistrar = KtxJavaSerializableComponentRegistrar(),
): JvmCompilationResult = KotlinCompilation().apply {
    sources = listOf(sourceFile)
    compilerPluginRegistrars = listOf(plugin)
    inheritClassPath = true
    messageOutputStream = System.out // For debugging - shows plugin log messages
}.compile()

fun JvmCompilationResult.loadClass(className: String): Class<*> = classLoader.loadClass(className)

/**
 * Compiles the given source code string with the KtxJavaSerializable compiler plugin enabled.
 *
 * @param sourceCode The Kotlin source code to compile
 * @param fileName The name to use for the source file (defaults to "TestFile.kt")
 * @param plugin The compiler plugin registrar to use (defaults to KtxJavaSerializableComponentRegistrar)
 * @return The compilation result
 */
@OptIn(ExperimentalCompilerApi::class)
fun compile(
    sourceCode: String,
    fileName: String = "TestFile.kt",
    plugin: CompilerPluginRegistrar = KtxJavaSerializableComponentRegistrar(),
): JvmCompilationResult = compile(SourceFile.kotlin(fileName, sourceCode), plugin)

/**
 * Extension function to get the companion object class from a compiled class.
 *
 * @param companionName The name of the companion object (defaults to "Companion")
 * @return The companion object's Class, or null if not found
 */
fun Class<*>.getCompanionClass(companionName: String = "Companion"): Class<*>? =
    declaredClasses.firstOrNull { it.simpleName == companionName }

/**
 * Extension function to check if a field has the expected modifiers.
 *
 * @param modifiers The modifiers to check (from java.lang.reflect.Modifier)
 * @return True if all specified modifiers are present
 */
fun Field.hasModifiers(vararg modifiers: Int): Boolean =
    modifiers.all { (this.modifiers and it) != 0 }
