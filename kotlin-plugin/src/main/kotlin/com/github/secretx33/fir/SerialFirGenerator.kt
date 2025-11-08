package com.github.secretx33.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.codegen.JvmCodegenUtil.isConst
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
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
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class SerialFirGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
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
     *  1. Step 1 (getNestedClassifiersNames): "These nested classifiers will exist"
     *  2. Step 2 (generateClassLikeDeclaration): "Here's the actual implementation of that nested classifier"
     */
    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        val superClassifiers = super.getNestedClassifiersNames(classSymbol, context)
        if (!implementsSerializable(classSymbol)) return superClassifiers

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
        if (name != COMPANION_NAME || !implementsSerializable(owner)) {
            return null
        }

        // Create empty companion object (its properties will be added later in getCallableNamesForClass)
        return createCompanionObject(owner, predicateKey).symbol
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        val superCallableNames = super.getCallableNamesForClass(classSymbol, context)

        // Only generate for companion objects of Serializable classes
        classSymbol.takeIf { it.isCompanion }
            ?.getContainingClassSymbol()
            ?.takeIf(::implementsSerializable)
            ?: return superCallableNames

        // Check if serialVersionUID already exists
        val hasSerialVersionUID = classSymbol.declarationSymbols.any {
            it is FirPropertySymbol && it.name == SERIAL_VERSION_UID_NAME
        }
        return superCallableNames + setOfNotNull(SERIAL_VERSION_UID_NAME.takeIf { !hasSerialVersionUID })
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> {
        val owner = context?.owner ?: return emptyList()
        if (callableId.callableName != SERIAL_VERSION_UID_NAME) return emptyList()
        if (!owner.isCompanion) return emptyList()

        val outerClass = owner.getContainingClassSymbol(session) ?: return emptyList()
        if (!implementsSerializable(outerClass)) return emptyList()

        val property = createMemberProperty(
            owner = owner,
            key = predicateKey,
            name = SERIAL_VERSION_UID_NAME,
            returnType = session.builtinTypes.longType.type,
            isVal = true
        ) {
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Private,
                Modality.FINAL,
                effectiveVisibility = Visibilities.Private.toEffectiveVisibility(owner, forClass = false)
            )

            // Mark as const
            isConst = true

            // Set initial value to 1L (you could implement proper serialVersionUID calculation here)
            // For proper calculation, you'd need to hash class structure like Java does
        }

        return listOf(property.symbol)
    }

    private fun implementsSerializable(classSymbol: FirClassLikeSymbol<*>): Boolean {
        if (classSymbol !is FirRegularClassSymbol) return false

        return classSymbol.resolvedSuperTypes.any { superType ->
            (superType as? ConeClassLikeType)?.lookupTag?.classId == SERIALIZABLE_CLASS_ID
        }
    }
}
