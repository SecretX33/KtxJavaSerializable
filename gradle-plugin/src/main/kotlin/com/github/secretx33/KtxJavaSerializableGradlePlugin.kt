package com.github.secretx33

import org.gradle.api.Plugin
import org.gradle.api.Project

class KtxJavaSerializableGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("ktxJavaSerializable", KtxJavaSerializableExtension::class.java)
//        project.extensions.add("ktxJavaSerializable", com.github.secretx33.KtxJavaSerializableExtension())
//        project.tasks.register("ktxJavaSerializable", KtxJavaSerializableTask::class.java) {
//            it.group = "com.github.secretx33"
//            it.description = "Generate Java serializable classes from Kotlin data classes"
//            it.ktxJavaSerializable.project = project
//        }
    }
}

open class KtxJavaSerializableExtension {
    var enabled: Boolean = true
}
