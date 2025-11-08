dependencies {
    implementation(project(":common"))
    compileOnly(kotlin("compiler"))
    compileOnly("com.google.auto.service:auto-service:1.1.1")
    kapt("com.google.auto.service:auto-service:1.1.1")
    testImplementation(kotlin("test-junit"))
    testImplementation(kotlin("compiler"))
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.6.0")
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
    }
}
