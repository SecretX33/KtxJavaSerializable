package com.github.secretx33.codegen

import com.github.secretx33.util.shouldGenerateSerialVersionProperty
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor

class KtxJavaSerializableClassGenerationInterceptor : ClassBuilderInterceptorExtension {

    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink,
    ): ClassBuilderFactory = object : ClassBuilderFactory by interceptedFactory {
        override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder =
            KtxJavaSerializableClassBuilder(origin, interceptedFactory.newClassBuilder(origin))
    }
}

class KtxJavaSerializableClassBuilder(
    classOrigin: JvmDeclarationOrigin,
    private val delegateBuilder: ClassBuilder,
) : DelegatingClassBuilder() {

    private val classDescriptor = classOrigin.descriptor as ClassDescriptor

    override fun getDelegate(): ClassBuilder = delegateBuilder

    override fun newMethod(origin: JvmDeclarationOrigin, access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)
        if (!classDescriptor.shouldGenerateSerialVersionProperty()) {
            return original
        }
        return SerialVersionMethodVisitor(classDescriptor, original)
    }
}
