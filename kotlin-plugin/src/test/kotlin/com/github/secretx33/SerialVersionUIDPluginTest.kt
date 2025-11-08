package com.github.secretx33

import com.tschuchort.compiletesting.KotlinCompilation
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for the KtxJavaSerializable compiler plugin.
 *
 * This plugin automatically generates the `serialVersionUID` field for Kotlin classes
 * implementing `java.io.Serializable`.
 */
class SerialVersionUIDPluginTest {

    @Test
    fun `plugin generates serialVersionUID for Serializable class without companion`() {
        val result = compile("""
            class MySerializableClass : java.io.Serializable {
                val name: String = "test"
            }
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        // Load the compiled class
        val clazz = result.loadClass("MySerializableClass")

        // Verify companion object exists
        val companionClass = clazz.getCompanionClass()
        assertNotNull(companionClass, "Companion object should be generated for Serializable class")

        // Verify serialVersionUID field exists
        val serialVersionUIDField = companionClass.getDeclaredField("serialVersionUID")
        assertNotNull(serialVersionUIDField, "serialVersionUID field should exist in companion object")

        // Verify field is private, static, and final
        assertTrue(Modifier.isPrivate(serialVersionUIDField.modifiers), "serialVersionUID should be private")
        assertTrue(Modifier.isStatic(serialVersionUIDField.modifiers), "serialVersionUID should be static")
        assertTrue(Modifier.isFinal(serialVersionUIDField.modifiers), "serialVersionUID should be final")

        // Verify field type is Long
        assertEquals(Long::class.javaPrimitiveType, serialVersionUIDField.type, "serialVersionUID should be of type Long")

        // Verify field value is the default (1L)
        serialVersionUIDField.isAccessible = true
        assertEquals(1L, serialVersionUIDField.getLong(null), "serialVersionUID should have default value of 1L")
    }

    @Test
    fun `plugin adds serialVersionUID to existing companion object`() {
        val result = compile("""
            class MySerializableClass : java.io.Serializable {
                companion object {
                    const val OTHER_CONSTANT = "test"
                }
            }
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val clazz = result.loadClass("MySerializableClass")
        val companionClass = clazz.getCompanionClass()
        assertNotNull(companionClass, "Companion object should exist")

        // Verify both fields exist
        val serialVersionUID = companionClass.getDeclaredField("serialVersionUID")
        val otherConstant = companionClass.getDeclaredField("OTHER_CONSTANT")

        assertNotNull(serialVersionUID, "serialVersionUID should be added to existing companion")
        assertNotNull(otherConstant, "Existing companion fields should be preserved")

        serialVersionUID.isAccessible = true
        assertEquals(1L, serialVersionUID.getLong(null))

        otherConstant.isAccessible = true
        assertEquals("test", otherConstant.get(null))
    }

    @Test
    fun `plugin skips classes with explicit serialVersionUID`() {
        val result = compile("""
            class MySerializableClass : java.io.Serializable {
                companion object {
                    private const val serialVersionUID = 42L
                }
            }
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val clazz = result.loadClass("MySerializableClass")
        val companionClass = clazz.getCompanionClass()!!
        val serialVersionUID = companionClass.getDeclaredField("serialVersionUID")

        serialVersionUID.isAccessible = true
        // Should preserve user's explicit value, not overwrite with default
        assertEquals(42L, serialVersionUID.getLong(null), "User's explicit serialVersionUID should be preserved")
    }

    @Test
    fun `plugin skips non-Serializable classes`() {
        val result = compile("""
            class MyNormalClass {
                val name: String = "test"
            }
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val clazz = result.loadClass("MyNormalClass")

        // Should not have a companion object generated
        val companionClass = clazz.getCompanionClass()
        assertNull(companionClass, "Non-Serializable class should not have companion object generated")
    }

    @Test
    fun `plugin works with private companion object`() {
        val result = compile("""
            class MySerializableClass : java.io.Serializable {
                private companion object {
                    const val OTHER = 123
                }
            }
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val clazz = result.loadClass("MySerializableClass")
        val companionClass = clazz.getCompanionClass()
        assertNotNull(companionClass, "Private companion should still receive serialVersionUID")

        // Verify serialVersionUID was added
        val serialVersionUID = companionClass.getDeclaredField("serialVersionUID")
        assertNotNull(serialVersionUID, "serialVersionUID should be added to private companion")

        serialVersionUID.isAccessible = true
        assertEquals(1L, serialVersionUID.getLong(null))
    }

    @Test
    fun `plugin handles nested Serializable classes`() {
        val result = compile("""
            class OuterClass : java.io.Serializable {
                class InnerClass : java.io.Serializable
            }
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        // Verify both classes have serialVersionUID
        val outerClass = result.loadClass("OuterClass")
        val innerClass = result.loadClass("OuterClass\$InnerClass")

        val outerCompanion = outerClass.getCompanionClass()
        val innerCompanion = innerClass.getCompanionClass()

        assertNotNull(outerCompanion, "Outer Serializable class should have companion")
        assertNotNull(innerCompanion, "Inner Serializable class should have companion")

        val outerUID = outerCompanion.getDeclaredField("serialVersionUID")
        val innerUID = innerCompanion.getDeclaredField("serialVersionUID")

        assertNotNull(outerUID, "Outer class should have serialVersionUID")
        assertNotNull(innerUID, "Inner class should have serialVersionUID")

        outerUID.isAccessible = true
        innerUID.isAccessible = true

        assertEquals(1L, outerUID.getLong(null))
        assertEquals(1L, innerUID.getLong(null))
    }

    @Test
    fun `plugin handles class with properties and methods`() {
        val result = compile("""
            class ComplexClass(
                val id: Int,
                var name: String
            ) : java.io.Serializable {
                fun doSomething() = "result"

                val computed: String
                    get() = "computed_${"$"}name"
            }
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val clazz = result.loadClass("ComplexClass")
        val companionClass = clazz.getCompanionClass()

        assertNotNull(companionClass)

        val serialVersionUID = companionClass.getDeclaredField("serialVersionUID")
        assertNotNull(serialVersionUID)

        serialVersionUID.isAccessible = true
        assertEquals(1L, serialVersionUID.getLong(null))
    }

    @Test
    fun `plugin handles data class implementing Serializable`() {
        val result = compile("""
            data class DataClass(
                val id: Int,
                val name: String
            ) : java.io.Serializable
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val clazz = result.loadClass("DataClass")
        val companionClass = clazz.getCompanionClass()

        assertNotNull(companionClass, "Data class should receive companion object")

        val serialVersionUID = companionClass.getDeclaredField("serialVersionUID")
        assertNotNull(serialVersionUID)

        serialVersionUID.isAccessible = true
        assertEquals(1L, serialVersionUID.getLong(null))
    }

    @Test
    fun `plugin handles class with named companion`() {
        val result = compile("""
            class MySerializableClass : java.io.Serializable {
                companion object Factory {
                    fun create() = MySerializableClass()
                }
            }
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val clazz = result.loadClass("MySerializableClass")
        // Named companions still have the simple name "Companion"
        val companionClass = clazz.getCompanionClass()

        assertNotNull(companionClass, "Named companion should still receive serialVersionUID")

        val serialVersionUID = companionClass.getDeclaredField("serialVersionUID")
        assertNotNull(serialVersionUID)

        serialVersionUID.isAccessible = true
        assertEquals(1L, serialVersionUID.getLong(null))
    }

    @Test
    fun `plugin handles multiple Serializable classes in same file`() {
        val result = compile("""
            class FirstClass : java.io.Serializable {
                val first: Int = 1
            }

            class SecondClass : java.io.Serializable {
                val second: Int = 2
            }

            class ThirdClass {
                // Not Serializable
                val third: Int = 3
            }
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        // Verify first Serializable class
        val firstClass = result.loadClass("FirstClass")
        val firstCompanion = firstClass.getCompanionClass()
        assertNotNull(firstCompanion)
        assertNotNull(firstCompanion.getDeclaredField("serialVersionUID"))

        // Verify second Serializable class
        val secondClass = result.loadClass("SecondClass")
        val secondCompanion = secondClass.getCompanionClass()
        assertNotNull(secondCompanion)
        assertNotNull(secondCompanion.getDeclaredField("serialVersionUID"))

        // Verify non-Serializable class has no companion
        val thirdClass = result.loadClass("ThirdClass")
        val thirdCompanion = thirdClass.getCompanionClass()
        assertNull(thirdCompanion, "Non-Serializable class should not get companion")
    }

    @Test
    fun `plugin handles class implementing Serializable indirectly`() {
        val result = compile("""
            interface MyInterface : java.io.Serializable

            class IndirectlySerializable : MyInterface {
                val data: String = "test"
            }
        """)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val clazz = result.loadClass("IndirectlySerializable")
        val companionClass = clazz.getCompanionClass()

        assertNotNull(companionClass, "Class indirectly implementing Serializable should get companion")

        val serialVersionUID = companionClass.getDeclaredField("serialVersionUID")
        assertNotNull(serialVersionUID)

        serialVersionUID.isAccessible = true
        assertEquals(1L, serialVersionUID.getLong(null))
    }
}
