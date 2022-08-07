plugins {
    id("java-gradle-plugin")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin-api"))
    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}

gradlePlugin {
    plugins {
        create("ktx-java-serializable").apply {
            id = "ktx-java-serializable"
            displayName = "KtxJavaSerializable"
            description = "Automatically generates serialVersionUID property on Kotlin classes implementing Java Serializable interface"
            implementationClass = "com.github.secretx33.KtxJavaSerializableGradlePlugin"
        }
    }
}
