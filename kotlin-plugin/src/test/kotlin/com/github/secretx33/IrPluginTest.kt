package com.github.secretx33
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.JvmDefaultMode
import org.junit.Test
import kotlin.test.assertEquals

class IrPluginTest {
    @Test
    fun `IR plugin success`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "main.kt", """
fun main() {
  println(debug())
}
fun debug() = "Hello, World!"

class MyClassWithSerial(
  val props: Int,
) : java.io.Serializable {
//  private companion object {
//    const val serialVersionUID = 1L
//  }
}
"""
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}

@Suppress("UNCHECKED_CAST")
fun compile(
    sourceFiles: List<SourceFile>,
    plugin: CompilerPluginRegistrar = KtxJavaSerializableComponentRegistrar(),
): KotlinCompilation.Result = KotlinCompilation().apply {
    sources = sourceFiles
    compilerPluginRegistrars = listOf(plugin)
    inheritClassPath = true
    jvmDefault = JvmDefaultMode.ALL_COMPATIBILITY.description
    jvmTarget = "1.8"
}.compile()

fun compile(
    sourceFile: SourceFile,
    plugin: CompilerPluginRegistrar = KtxJavaSerializableComponentRegistrar(),
): KotlinCompilation.Result = compile(listOf(sourceFile), plugin)
