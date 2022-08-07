package com.github.secretx33
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
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

fun compile(
    sourceFiles: List<SourceFile>,
    plugin: ComponentRegistrar = KtxJavaSerializableComponentRegistrar(),
): KotlinCompilation.Result =
    KotlinCompilation().apply {
        sources = sourceFiles
        useIR = true
        compilerPlugins = listOf(plugin)
        inheritClassPath = true
    }.compile()

fun compile(
    sourceFile: SourceFile,
    plugin: ComponentRegistrar = KtxJavaSerializableComponentRegistrar(),
): KotlinCompilation.Result = compile(listOf(sourceFile), plugin)
