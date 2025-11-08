package com.github.secretx33.fir

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
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class SerialFirGenerator(
    session: FirSession,
    private val messageCollector: MessageCollector,
    private val defaultSerialVersionUID: Long,
) : FirDeclarationGenerationExtension(session) {
    private companion object {
        val SERIALIZABLE_CLASS_ID = ClassId.topLevel(FqName("java.io.Serializable"))
        val SERIAL_VERSION_UID_NAME = Name.identifier("serialVersionUID")
        val COMPANION_NAME = Name.identifier("Companion")
    }

    private val predicateKey = object : GeneratedDeclarationKey()

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
        return superClassifiers + setOfNotNull(COMPANION_NAME.takeIf { !companionClassExists })
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
        if (name != COMPANION_NAME || !owner.implementsSerializable()) {
            return null
        }

        // Create empty companion object (its properties will be added later in getCallableNamesForClass)
        return createCompanionObject(owner, predicateKey).symbol
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

        // Set initial value to 1L (you could implement proper serialVersionUID calculation here)
        // For proper calculation, you'd need to hash class structure like Java does
        val property = createMemberProperty(
            owner = owner,
            key = predicateKey,
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
