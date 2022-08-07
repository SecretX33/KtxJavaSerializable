//package com.github.secretx33.codegen
//
//import com.github.secretx33.SERIALIZABLE_FQ_PROPERTY_NAME
//import com.github.secretx33.SERIALIZABLE_OPTCODES
//import com.github.secretx33.util.shouldGenerateSerialVersionProperty
//import org.jetbrains.kotlin.codegen.FunctionGenerationStrategy
//import org.jetbrains.kotlin.codegen.ImplementationBodyCodegen
//import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
//import org.jetbrains.kotlin.com.intellij.psi.CommonClassNames.SERIAL_VERSION_UID_FIELD_NAME
//import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
//import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
//import org.jetbrains.kotlin.descriptors.ClassDescriptor
//import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
//import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
//import org.jetbrains.kotlin.descriptors.Modality
//import org.jetbrains.kotlin.descriptors.PropertyDescriptor
//import org.jetbrains.kotlin.descriptors.SourceElement
//import org.jetbrains.kotlin.descriptors.annotations.Annotations
//import org.jetbrains.kotlin.descriptors.impl.ClassConstructorDescriptorImpl
//import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
//import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
//import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
//import org.jetbrains.kotlin.resolve.jvm.AsmTypes
//import org.jetbrains.kotlin.resolve.jvm.annotations.findJvmOverloadsAnnotation
//import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
//import org.jetbrains.org.objectweb.asm.Opcodes
//import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
//
//class KtxJavaSerializableExpressionCodegenExtension : ExpressionCodegenExtension {
//
//    override val shouldGenerateClassSyntheticPartsInLightClassesMode = true
//
//    override fun generateClassSyntheticParts(codegen: ImplementationBodyCodegen) = codegen.run {
//        if (descriptor.shouldGenerateSerialVersionProperty()) {
//            generateSerialVersionProperty()
//        }
//    }
//
//    private fun ImplementationBodyCodegen.generateSerialVersionProperty() {
//        val constructorDescriptor = createNoArgConstructorDescriptor(descriptor)
//
//        // If a parent sealed class has not a zero-parameter constructor, user must write @NoArg annotation for the parent class as well,
//        // and then we generate <init>()V
//        functionCodegen.generateMethod(JvmDeclarationOrigin.NO_ORIGIN, constructorDescriptor, object : FunctionGenerationStrategy.CodegenBased(state) {
//            override fun doGenerateBody(codegen: ExpressionCodegen, signature: JvmMethodSignature) {
//                codegen.v.load(0, AsmTypes.OBJECT_TYPE)
//
//                if (isParentASealedClassWithDefaultConstructor) {
//                    codegen.v.aconst(null)
//                    codegen.v.visitMethodInsn(
//                        Opcodes.INVOKESPECIAL, superClassInternalName, "<init>",
//                        "(Lkotlin/jvm/internal/DefaultConstructorMarker;)V", false
//                    )
//                } else {
//                    codegen.v.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassInternalName, "<init>", "()V", false)
//                }
//
//                if (invokeInitializers) {
//                    generateInitializers(codegen)
//                }
//                codegen.v.visitInsn(Opcodes.RETURN)
//            }
//        })
//
//
//        val selfType = typeMapper.mapType(descriptor)
//        val serialVersionDescriptor = createSerialVersionUidProperty(descriptor)
//        val visitor = v.newMethod(
//            JvmDeclarationOrigin.NO_ORIGIN,
//            SERIALIZABLE_OPTCODES,
//            SERIALIZABLE_READ,
//            ,
//            null,
//            DEFAULT_VALUE,
//        )
//        InstructionAdapter(visitor).apply {
//            load(Opcodes.LCONST_1, selfType)
//            putstatic(selfType.internalName, SERIAL_VERSION_UID_FIELD_NAME, "J")
//        }
//        visitor.visitEnd()
//    }
//
//    private fun ImplementationBodyCodegen.generateNoArgsContructor() {
//        val superClassInternalName = typeMapper.mapClass(descriptor.getSuperClassOrAny()).internalName
//
//        val constructorDescriptor = createNoArgConstructorDescriptor(descriptor)
//
//        val superClass = descriptor.getSuperClassOrAny()
//
//        // If a parent sealed class has not a zero-parameter constructor, user must write @NoArg annotation for the parent class as well,
//        // and then we generate <init>()V
//        val isParentASealedClassWithDefaultConstructor =
//            superClass.modality == Modality.SEALED && superClass.constructors.any { isZeroParameterConstructor(it) }
//
//        functionCodegen.generateMethod(JvmDeclarationOrigin.NO_ORIGIN, constructorDescriptor, object : FunctionGenerationStrategy.CodegenBased(state) {
//            override fun doGenerateBody(codegen: ExpressionCodegen, signature: JvmMethodSignature) {
//                codegen.v.load(0, AsmTypes.OBJECT_TYPE)
//
//                if (isParentASealedClassWithDefaultConstructor) {
//                    codegen.v.aconst(null)
//                    codegen.v.visitMethodInsn(
//                        Opcodes.INVOKESPECIAL, superClassInternalName, "<init>",
//                        "(Lkotlin/jvm/internal/DefaultConstructorMarker;)V", false
//                    )
//                } else {
//                    codegen.v.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassInternalName, "<init>", "()V", false)
//                }
//
//                if (invokeInitializers) {
//                    generateInitializers(codegen)
//                }
//                codegen.v.visitInsn(Opcodes.RETURN)
//            }
//        })
//    }
//
//    private companion object {
//        fun createSerialVersionUidProperty(containingClass: ClassDescriptor): PropertyDescriptor =
//            PropertyDescriptorImpl.create(
//                containingClass,
//                Annotations.EMPTY,
//                Modality.FINAL,
//                DescriptorVisibilities.PRIVATE,
//                false,  // isVar
//                SERIALIZABLE_FQ_PROPERTY_NAME.shortName(),
//                CallableMemberDescriptor.Kind.SYNTHESIZED,
//                SourceElement.NO_SOURCE,  // means that this code did not come from a file
//                false,  // lateInit
//                true,  // isConst
//                false,  // isExpect
//                false,  // isActual
//                false,  // isExternal
//                false,  // isDelegated
//            )
//
//        fun isZeroParameterConstructor(constructor: ClassConstructorDescriptor): Boolean {
//            val parameters = constructor.valueParameters
//            return parameters.isEmpty() ||
//                (parameters.all { it.declaresDefaultValue() } && (constructor.isPrimary || constructor.findJvmOverloadsAnnotation() != null))
//        }
//
//        fun createNoArgConstructorDescriptor(containingClass: ClassDescriptor): ConstructorDescriptor =
//            ClassConstructorDescriptorImpl.createSynthesized(containingClass, Annotations.EMPTY, false, SourceElement.NO_SOURCE).apply {
//                initialize(
//                    null,
//                    calculateDispatchReceiverParameter(),
//                    emptyList(),
//                    emptyList(),
//                    emptyList(),
//                    containingClass.builtIns.unitType,
//                    Modality.OPEN,
//                    DescriptorVisibilities.PUBLIC
//                )
//            }
//
//        fun createStaticConstructorDescriptor(containingClass: ClassDescriptor): ConstructorDescriptor =
//            ClassConstructorDescriptorImpl.createSynthesized(containingClass, Annotations.EMPTY, false, SourceElement.NO_SOURCE).apply {
//                initialize(
//                    null,
//                    calculateDispatchReceiverParameter(),
//                    emptyList(),
//                    emptyList(),
//                    emptyList(),
//                    containingClass.builtIns.unitType,
//                    Modality.OPEN,
//                    DescriptorVisibilities.PUBLIC
//                )
//            }
//    }
//}
