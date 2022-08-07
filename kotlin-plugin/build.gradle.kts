dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(kotlin("compiler-embeddable"))
    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
    testImplementation(kotlin("test-junit"))
    testImplementation(kotlin("compiler-embeddable"))
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
}
