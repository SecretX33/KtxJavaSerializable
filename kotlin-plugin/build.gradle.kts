dependencies {
    implementation(project(":common"))
    compileOnly(kotlin("compiler"))
    compileOnly("com.google.auto.service:auto-service:1.1.1")
    kapt("com.google.auto.service:auto-service:1.1.1")

    // Test dependencies
    testImplementation(kotlin("test"))  // JUnit 5 included
    testImplementation(kotlin("compiler"))
    testImplementation("dev.zacsweers.kctfork:core:0.11.0")  // Updated fork with K2 support
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
    }
}

tasks.test {
    useJUnitPlatform()  // Enable JUnit 5

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true  // See println and plugin log output
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }

    // Increase heap size for compiler tests
    minHeapSize = "512m"
    maxHeapSize = "2g"
}
