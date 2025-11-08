import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0" apply false
    kotlin("kapt") version "2.1.0" apply false
    id("com.gradle.plugin-publish") version "1.0.0" apply false
}

allprojects {
    group = "com.github.secretx33"
    version = "0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-kapt")

    val javaVersion = JvmTarget.JVM_1_8

//    dependencies {
//        testImplementation(kotlin("test"))
//    }
//
//    tasks.test {
//        useJUnitPlatform()
//    }

    tasks.withType<JavaCompile> {
        options.apply {
            release.set(javaVersion.name.substringAfterLast("_").toInt())
            encoding = "UTF-8"
        }
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
}
