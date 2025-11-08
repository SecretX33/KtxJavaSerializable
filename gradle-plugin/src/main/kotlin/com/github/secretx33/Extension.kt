package com.github.secretx33

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation

internal val KotlinCompilation<*>.project: Project
    get() = target.project

internal inline fun <reified T : Any> ExtensionContainer.getByType(): T =
    getByType(T::class.java)
