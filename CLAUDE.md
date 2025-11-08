# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

KtxJavaSerializable is a Kotlin compiler plugin that automatically generates `serialVersionUID` properties for Kotlin classes implementing Java's `Serializable` interface. The plugin uses the Kotlin K2 (FIR) compiler architecture.

## Project Structure

This is a multi-module Gradle project with the following modules:

- **common**: Shared configuration and utilities used by both plugin modules (currently empty)
- **kotlin-plugin**: The actual Kotlin compiler plugin implementation using K2/FIR API
- **gradle-plugin**: Gradle plugin wrapper that integrates the compiler plugin into the build system
- **test**: Sample test project for manual testing

## Building and Testing

### Build Commands

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :kotlin-plugin:build
./gradlew :gradle-plugin:build

# Clean build
./gradlew clean build
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :kotlin-plugin:test

# Run single test
./gradlew :kotlin-plugin:test --tests "com.github.secretx33.IrPluginTest"
```

The kotlin-plugin uses `kotlin-compile-testing` library for integration tests that compile Kotlin code with the plugin enabled.

## Architecture

### Kotlin Compiler Plugin (kotlin-plugin)

This module implements a K2 (FIR) compiler plugin using the following components:

1. **FIR Extension Registration** (`SerialFirExtensionRegistrar`): Entry point that registers FIR extensions with the compiler
2. **FIR Code Generation** (`SerialFirGenerator`): Extends `FirDeclarationGenerationExtension` to:
   - Generate companion objects for classes implementing `Serializable`
   - Generate `serialVersionUID` properties within those companion objects
3. **Component Registrar** (`KtxJavaSerializableComponentRegistrar`): Registers the plugin with the Kotlin compiler
4. **Command Line Processor** (`KtxJavaSerializableCommandLineProcessor`): Handles plugin configuration from build system

The plugin works in two phases:
- **Declaration Phase**: Declares what members will exist (companion object + serialVersionUID property)
- **Generation Phase**: Creates the actual FIR declarations with proper types, visibility, and modifiers

### Gradle Plugin (gradle-plugin)

Thin wrapper that:
- Registers as a Kotlin compiler plugin (`KotlinCompilerPluginSupportPlugin`)
- Creates the `ktxJavaSerializable` extension for user configuration
- Passes configuration options to the compiler plugin
- Only applies to JVM-targeted Kotlin compilations

### Plugin Configuration

Users can configure the plugin in their `build.gradle.kts`:

```kotlin
ktxJavaSerializable {
    enabled = true
    defaultSerialVersionUID = 1L
}
```

## Working with Kotlin Compiler (K2/FIR)

### Important Documentation

When working on code that interacts with the Kotlin compiler's FIR (Frontend IR) system, **always read** the documentation in `@docs/ai/kotlin/` first:

- `docs/ai/kotlin/fir-basics.md`: Essential FIR concepts, phases, elements, and providers
- `docs/ai/kotlin/fir-plugins.md`: Complete FIR plugin API documentation including all extension points
- `docs/ai/kotlin/delegated_property_inference.md`: Details on delegated property type inference

These documents explain:
- FIR compilation phases and their order
- How to use `FirDeclarationGenerationExtension` correctly
- The two-step generation process (declaration → implementation)
- Contracts for generated declarations (must be fully resolved)
- Symbol-based access patterns (use symbols, not direct FIR access)

### Key FIR Concepts for This Project

1. **Compiler Phases**: The plugin operates during specific phases:
   - `SUPERTYPES`: When we check if a class implements `Serializable`
   - `STATUS`: When we generate the companion and property

2. **Symbol-Based Access**: Always access declaration properties through symbols, not direct FIR elements:
   ```kotlin
   val returnType = functionSymbol.resolvedReturnTypeRef  // Correct
   // NOT: functionSymbol.fir.returnTypeRef
   ```

3. **Generated Declaration Requirements**:
   - All generated declarations must be fully resolved
   - Use `FirDeclarationOrigin.Plugin` with a plugin key
   - Status must be `FirResolvedDeclarationStatus`
   - All type refs must be `FirResolvedTypeRef`

## Development Notes

- **Kotlin Version**: 2.1.0
- **JVM Target**: Java 8 (JVM 1.8)
- **Compiler Options**: Uses `-Xjsr305=strict` and `-Xjvm-default=all`
- **K2 Only**: This plugin only supports K2 compiler (`supportsK2 = true`)
- **Auto Service**: Uses Google Auto Service for service loader registration

## Common Tasks

### Adding a New Configuration Option

1. Add the option to `KtxJavaSerializableConfiguration` in common module
2. Update `KtxJavaSerializableCommandLineProcessor` to parse it
3. Update `KtxJavaSerializableGradlePlugin.applyToCompilation()` to pass it
4. Access it via `Configuration` in the kotlin-plugin module

### Modifying Code Generation Logic

1. Review relevant FIR documentation in `docs/ai/kotlin/`
2. Modify `SerialFirGenerator` methods (remember the two-step process)
3. Update tests in `IrPluginTest` to verify behavior
4. Run `./gradlew :kotlin-plugin:test` to confirm changes

### Debugging the Plugin

The plugin logs to the Kotlin compiler's message collector. Look for log messages prefixed with plugin-specific markers in compilation output.
