package com.github.secretx33

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation

internal val KotlinCompilation<*>.project: Project
    get() = target.project
