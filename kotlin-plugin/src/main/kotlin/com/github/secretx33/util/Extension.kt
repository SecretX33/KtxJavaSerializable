package com.github.secretx33.util

import java.util.EnumSet

internal inline fun <reified T : Enum<T>> enumSetOf(vararg values: T): Set<T> = EnumSet.noneOf(T::class.java).apply { addAll(values) }
