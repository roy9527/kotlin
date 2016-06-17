/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.java.model.internal

import com.intellij.psi.*
import org.jetbrains.kotlin.asJava.KtLightAnnotation
import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.Opcodes.*
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import java.net.URL
import java.net.URLClassLoader

internal fun createAnnotation(annotation: PsiAnnotation, annotationClass: PsiClass, classLoader: ClassLoader): Annotation? {
    val qualifiedName = annotationClass.qualifiedName ?: return null
    val implQualifiedName = "stub." + qualifiedName + ".Impl" + Integer.toHexString(annotation.text.hashCode())
    
    val annotationInternalName = qualifiedName.replace('.', '/')
    val implInternalName = implQualifiedName.replace('.', '/')

    val bytes = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS).apply {
        visit(V1_6, ACC_SUPER, implInternalName, null, "java/lang/Object", arrayOf(annotationInternalName))

        with(visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)) {
            visitCode()
            visitVarInsn(ALOAD, 0)
            visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
            visitInsn(RETURN)
            visitMaxs(-1, -1)
            visitEnd()
        }

        with(visitMethod(ACC_PUBLIC, "annotationType", "()Ljava/lang/Class;",
                         "()Ljava/lang/Class<+Ljava/lang/annotation/Annotation;>;", null)) {
            visitCode()
            visitLdcInsn(Type.getType("L$annotationInternalName;"))
            visitInsn(ARETURN)
            visitMaxs(-1, -1)
            visitEnd()
        }

        if (annotationClass.allMethods.isNotEmpty()) {
            val evaluator = JavaPsiFacade.getInstance(annotation.project).constantEvaluationHelper

            for (method in annotationClass.methods) {
                if (method !is PsiAnnotationMethod) continue

                if (method.returnType == null) continue
                val returnType = method.returnType!!

                val value = annotation.findAttributeValue(method.name) ?: method.defaultValue

                when (returnType) {
                    PsiType.VOID -> error("Unexpected type: void`")
                    PsiType.NULL -> error("Unexpected type: null")
                    is PsiClassType -> {
                        val resolvedQualifiedName = returnType.resolve()?.qualifiedName
                        if (resolvedQualifiedName == "java.lang.String") 
                            writeStringMethod(method, calculatePrimitiveValue(value, evaluator))
                        else 
                            error("Unsupported class type: $resolvedQualifiedName")
                    }
                    is PsiPrimitiveType -> writeLiteralMethod(method, calculatePrimitiveValue(value, evaluator))
                    is PsiArrayType -> writeArrayMethod(method, value, evaluator)
                    else -> error("Unexpected type: $returnType")
                }
            }
        }
        
        visitEnd()
    }.toByteArray()

    return loadClass(implQualifiedName, bytes, classLoader)?.newInstance() as? Annotation
}

private fun ClassWriter.writeStringMethod(method: PsiAnnotationMethod, value: Any?) {
    val stringValue = (value as? String) ?: null

    with (visitMethod(ACC_PUBLIC, method.name, "()Ljava/lang/String;", null, null)) {
        visitCode()
        if (stringValue != null) {
            visitLdcInsn(stringValue)
        } else {
            visitInsn(ACONST_NULL)
        }
        visitInsn(ARETURN)
        visitMaxs(-1,-1)
        visitEnd()
    }
}

private fun ClassWriter.writeArrayMethod(
        method: PsiAnnotationMethod, 
        value: PsiAnnotationMemberValue?,
        evaluator: PsiConstantEvaluationHelper
) {
    val componentType = (method.returnType as PsiArrayType).componentType
    val initializers = (value as? PsiArrayInitializerMemberValue)?.initializers ?: emptyArray()

    val arrayAsmType: Int
    var isAArray = false
    val storeInsn: Int
    val insn: Int
    
    val componentAsmType = when (componentType) {
        PsiType.BYTE -> { insn = BIPUSH; storeInsn = BASTORE; arrayAsmType = T_BYTE; Type.BYTE_TYPE }
        PsiType.SHORT -> { insn = SIPUSH; storeInsn = SASTORE; arrayAsmType = T_SHORT; Type.SHORT_TYPE }
        PsiType.INT -> { insn = SIPUSH; storeInsn = IASTORE; arrayAsmType = T_INT; Type.INT_TYPE }
        PsiType.CHAR -> { insn = BIPUSH; storeInsn = CASTORE; arrayAsmType = T_CHAR; Type.CHAR_TYPE }
        PsiType.BOOLEAN -> { insn = 0; storeInsn = BASTORE; arrayAsmType = T_BOOLEAN; Type.BOOLEAN_TYPE }
        PsiType.LONG -> { insn = 0; storeInsn = LASTORE; arrayAsmType = T_LONG; Type.LONG_TYPE }
        PsiType.FLOAT -> { insn = 0; storeInsn = FASTORE; arrayAsmType = T_FLOAT; Type.FLOAT_TYPE }
        PsiType.DOUBLE -> { insn = 0; storeInsn = DASTORE; arrayAsmType = T_DOUBLE; Type.DOUBLE_TYPE }
        is PsiClassType -> {
            val resolvedQualifiedName = componentType.resolve()?.qualifiedName
            if (resolvedQualifiedName == "java.lang.String") {
                insn = 0; storeInsn = AASTORE; isAArray = true
                arrayAsmType = T_INT; Type.getObjectType("java/lang/String")
            } else {
                error("Unexpected type: $resolvedQualifiedName")
            }
        }
        // TODO Enum, Annotation
        else -> error("Unexpected type: $componentType")
    }

    with (visitMethod(ACC_PUBLIC, method.name, "()[" + componentAsmType.descriptor, null, null)) {
        visitCode()
        visitIntInsn(SIPUSH, initializers.size)

        if (isAArray)
            visitTypeInsn(ANEWARRAY, componentAsmType.internalName)
        else
            visitIntInsn(NEWARRAY, arrayAsmType)
        
        initializers.forEachIndexed { index, value ->
            visitInsn(DUP)
            visitIntInsn(SIPUSH, index)
            
            val valueObject = when (value) {
                is KtLightAnnotation.LightExpressionValue<*> -> value.getConstantValue()
                is PsiLiteral -> value.value
                is PsiExpression -> evaluator.computeConstantExpression(value)
                else -> null
            }
            
            when (componentAsmType) {
                Type.BYTE_TYPE -> visitIntInsn(insn, byteValue(valueObject))
                Type.SHORT_TYPE -> visitIntInsn(insn, shortValue(valueObject))
                Type.INT_TYPE -> visitIntInsn(insn, intValue(valueObject))
                Type.CHAR_TYPE -> visitIntInsn(insn, charValue(valueObject))
                Type.BOOLEAN_TYPE -> visitInsn(if (valueObject == true) ICONST_1 else ICONST_0)
                Type.LONG_TYPE -> visitLdcInsn(longValue(valueObject))
                Type.FLOAT_TYPE -> visitLdcInsn(floatValue(valueObject))
                Type.DOUBLE_TYPE -> visitLdcInsn(doubleValue(valueObject))
                else -> {
                    if (componentAsmType.internalName == "java/lang/String") {
                        if (valueObject is String) {
                            visitLdcInsn(valueObject)
                        } else {
                            visitInsn(ACONST_NULL)
                        }
                    } else {
                        error("Unexpected type: $componentAsmType")
                    }
                }
            }
            
            visitInsn(storeInsn)
        }


        visitInsn(ARETURN)
        visitMaxs(-1,-1)
        visitEnd()
    }
}

private fun calculatePrimitiveValue(value: PsiAnnotationMemberValue?, evaluator: PsiConstantEvaluationHelper) = when (value) {
    is PsiLiteral -> value.value
    is PsiExpression -> evaluator.computeConstantExpression(value)
    else -> null
}

private fun ClassWriter.writeLiteralMethod(method: PsiAnnotationMethod, value: Any?) {
    val returnType = method.returnType!!
    
    val castValue: Any?
    val insn: Int
    val retInsn: Int

    val type = when (returnType) {
        PsiType.BYTE -> { insn = BIPUSH; retInsn = IRETURN; castValue = byteValue(value); Type.BYTE_TYPE }
        PsiType.SHORT -> { insn = SIPUSH; retInsn = IRETURN; castValue = shortValue(value); Type.SHORT_TYPE }
        PsiType.INT -> { insn = SIPUSH; retInsn = IRETURN; castValue = intValue(value); Type.INT_TYPE }
        PsiType.CHAR -> { insn = BIPUSH; retInsn = IRETURN; castValue = charValue(value); Type.CHAR_TYPE }
        PsiType.BOOLEAN -> { insn = 0; retInsn = IRETURN; castValue = booleanValue(value); Type.BOOLEAN_TYPE }
        PsiType.LONG -> { insn = 0; retInsn = LRETURN; castValue = longValue(value); Type.LONG_TYPE }
        PsiType.FLOAT -> { insn = 0; retInsn = FRETURN; castValue = floatValue(value); Type.FLOAT_TYPE }
        PsiType.DOUBLE -> { insn = 0; retInsn = DRETURN; castValue = doubleValue(value); Type.DOUBLE_TYPE }
        else -> error("Unexpected type $returnType")
    }
    
    with (visitMethod(ACC_PUBLIC, method.name, "()" + type.descriptor, null, null)) {
        visitCode()
        when (returnType) {
            PsiType.BOOLEAN -> visitInsn(if (castValue == true) ICONST_1 else ICONST_0)
            PsiType.LONG, PsiType.FLOAT, PsiType.DOUBLE -> visitLdcInsn(castValue)
            else -> visitIntInsn(insn, value as Int)
        }
        visitInsn(retInsn)
        visitMaxs(-1,-1)
        visitEnd()
    }
}

private fun byteValue(value: Any?): Int = (value as? Number)?.toByte()?.toInt() ?: 0
private fun intValue(value: Any?): Int = (value as? Number)?.toInt() ?: 0
private fun longValue(value: Any?): Long = (value as? Number)?.toLong() ?: 0
private fun shortValue(value: Any?): Int = (value as? Number)?.toShort()?.toInt() ?: 0
private fun floatValue(value: Any?): Float = (value as? Number)?.toFloat() ?: 0f
private fun doubleValue(value: Any?): Double = (value as? Number)?.toDouble() ?: 0.0
private fun booleanValue(value: Any?): Boolean = value as? Boolean ?: false
private fun charValue(value: Any?): Int = (value as? Char)?.toInt() ?: 0.toChar().toInt()

private fun loadClass(fqName: String, bytes: ByteArray, baseClassLoader: ClassLoader): Class<*>? {
    class ByteClassLoader(
            urls: Array<out URL>?,
            parent: ClassLoader?,
            private var extraClasses: MutableMap<String, ByteArray>
    ) : URLClassLoader(urls, parent) {
        override fun findClass(name: String): Class<*>? {
            return extraClasses.remove(name)?.let {
                defineClass(name, it, 0, it.size)
            } ?: super.findClass(name)
        }
    }

    try {
        val classLoader = ByteClassLoader(emptyArray(), baseClassLoader, hashMapOf(fqName to bytes))
        return Class.forName(fqName, false, classLoader)
    } catch (e: Throwable) {
        return null
    }
}