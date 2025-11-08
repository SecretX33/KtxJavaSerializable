package com.github.secretx33.fir

import com.github.secretx33.COMPANION_CLASS_NAME
import com.github.secretx33.SERIALIZABLE_CLASS_ID
import com.github.secretx33.SERIAL_VERSION_UID_NAME
import com.github.secretx33.util.info
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.plugin.createCompanionObject
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

/**
 *  ## What does this class do?
 *  Modifies Kotlin classes that implements [java.io.Serializable] but doesn't declare an explicit `serialVersionUID`
 *  property by adding a companion object (if missing) and a serialVersionUID property to it.
 *
 *  It automatically skips classes that already have a `serialVersionUID` property declared.
 *
 *  ## What is this class?
 *  This class is a FIR (Frontend Intermediate Representation) declaration generation extension for the Kotlin compiler,
 *  so it doesn't add any initializer to the property (because it can't), which means that the property has no
 *  value until the IR (Intermediate Representation) phase, where another extension adds the initializer.
 *
 *  ## Example
 *  ### Before
 *  Before applying this generator, a serializable class like:
 *  ```kt
 *  class MySerializableClass : java.io.Serializable {
 *  }
 *  ```
 *
 *  ### After
 *  After applying this generator, the class will be transformed to:
 *  ```kt
 *  class MySerializableClass : java.io.Serializable {
 *
 *    private companion object {  // ← The plugin adds a private companion object (if not present)
 *      private const val serialVersionUID: Long  // ← and the serialVersionUID property (note the absence of any value)
 *    }
 *  }
 *  ```
 */
class SerialFirGenerator(
    session: FirSession,
    private val messageCollector: MessageCollector,
) : FirDeclarationGenerationExtension(session) {
    private object DeclarationKey : GeneratedDeclarationKey()

    /**
     * Returns what nested classes or objects should exist inside this class.
     *
     * The contract:
     * - Input: A class symbol (the outer class)
     * - Output: A set of names representing nested classifiers (classes/objects) to generate
     * - Effect: For each name this function return, the compiler will later call it back to requesting
     * the actual implementation
     *
     *  It's part of a two-step generation process:
     *  1. Step 1: Declaration Phase (getNestedClassifiersNames): "These nested classifiers will exist"
     *  2. Step 2: Generation Phase (generateClassLikeDeclaration): "Here's the actual implementation of that nested classifier"
     */
    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        val superClassifiers = super.getNestedClassifiersNames(classSymbol, context)
        if (!classSymbol.implementsSerializable()) return superClassifiers

        // Check if companion object already exists
        val companionClassExists = classSymbol.declarationSymbols.any {
            it is FirRegularClassSymbol && it.isCompanion
        }
        return superClassifiers + setOfNotNull(COMPANION_CLASS_NAME.takeIf { !companionClassExists })
    }

    /**
     * Generates the actual nested class or object implementation.
     *
     * See: [getNestedClassifiersNames] KDoc for more details.
     */
    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext,
    ): FirClassLikeSymbol<*>? {
        // Try to get existing implementation from super
        super.generateNestedClassLikeDeclaration(owner, name, context)?.let { return it }

        // Only generate companion object for Serializable classes
        if (name != COMPANION_CLASS_NAME || !owner.implementsSerializable()) {
            return null
        }

        // Create empty companion object (its properties will be added later in getCallableNamesForClass)
        return createCompanionObject(owner, DeclarationKey) {
            visibility = Visibilities.Private
        }.symbol
    }

    /**
     * Returns what callable names (properties/functions/constructors) should exist inside this class.
     *
     * The contract:
     * - Input: A class symbol
     * - Output: A set of names representing callables to generate
     * - Effect: For each name this function return, the compiler will later call it back to requesting
     * the actual implementation
     *
     * It's part of a two-step generation process:
     * 1. Step 1: Declaration Phase (getCallableNamesForClass): "These callables will exist"
     * 2. Step 2 Generation Phase (generateProperties/generateFunctions): "Here's the actual implementation of that callable"
     */
    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        val superCallableNames = super.getCallableNamesForClass(classSymbol, context)

        // Only generate for companion objects of Serializable classes
        if (!classSymbol.isCompanionWithSerializableParent()) {
            return superCallableNames
        }

        // Check if serialVersionUID already exists
        val hasSerialVersionUID = classSymbol.declarationSymbols.any {
            it is FirPropertySymbol && it.name == SERIAL_VERSION_UID_NAME
        }
        return superCallableNames + setOfNotNull(SERIAL_VERSION_UID_NAME.takeIf { !hasSerialVersionUID })
    }

    /**
     * Generates the actual property implementation.
     *
     * See: [getCallableNamesForClass] KDoc for more details.
     */
    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> {
        val generatedProperties = super.generateProperties(callableId, context)
        val owner = context?.owner?.takeIf { owner ->
            callableId.callableName == SERIAL_VERSION_UID_NAME
                && owner.isCompanionWithSerializableParent()
                && generatedProperties.none { it.name == SERIAL_VERSION_UID_NAME }
        } ?: return generatedProperties

        // Add serialVersionUID property to companion object
        val property = createMemberProperty(
            owner = owner,
            key = DeclarationKey,
            name = SERIAL_VERSION_UID_NAME,
            returnType = session.builtinTypes.longType.coneType,
            isVal = true,
        ) {
            visibility = Visibilities.Private
            modality = Modality.FINAL
            status { isConst = true } // Const also makes the property static
        }

        messageCollector.info("${"-".repeat(100)}\n[MODIFIED!!] Name: ${owner.name}. Added property: ${property.name}")

        return listOf(property.symbol)
    }

    private fun FirClassLikeSymbol<*>?.isCompanionWithSerializableParent(): Boolean =
        this?.let {
            it.isCompanion && it.getContainingClassSymbol().implementsSerializable()
        } == true

    private fun FirClassLikeSymbol<*>?.implementsSerializable(): Boolean =
        (this as? FirClassSymbol<*>)?.let {
            resolvedSuperTypes.any { superType ->
                (superType as? ConeClassLikeType)?.lookupTag?.classId == SERIALIZABLE_CLASS_ID
            }
        } == true
}
