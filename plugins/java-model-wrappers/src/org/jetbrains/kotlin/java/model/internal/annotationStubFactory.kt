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

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiAnnotationMethod
import com.intellij.psi.PsiClass
import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.Opcodes.*
import java.net.URL
import java.net.URLClassLoader

internal fun createAnnotation(annotation: PsiAnnotation, annotationClass: PsiClass, classLoader: ClassLoader): Annotation? {
    val qualifiedName = annotationClass.qualifiedName ?: return null
    val annotationInternalName = qualifiedName.replace('.', '/')
    val implInternalName = "stub/AnnoImpl"
    
    val bytes = ClassWriter(ClassWriter.COMPUTE_MAXS).apply {
        visit(V1_6, ACC_SUPER, implInternalName, null, "java/lang/Object", arrayOf(annotationInternalName))
        
        with (visitMethod(0, "<init>", "()V", null, null)) {
            visitCode()
            visitVarInsn(ALOAD, 0)
            visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
            visitEnd()
        }
        
        with (visitMethod(ACC_PUBLIC, "annotationType", "()Ljava/lang/Class;",
                          "()Ljava/lang/Class<+Ljava/lang/annotation/Annotation;>;", null)) {
            visitCode()
            visitLdcInsn(Type.getType("L$annotationInternalName;"))
            visitInsn(ARETURN)
            visitEnd()
        }
        
        //TODO annotation methods
        for (method in annotationClass.allMethods) {
            if (method !is PsiAnnotationMethod) continue
            val value = annotation.findAttributeValue(method.name) ?: method.defaultValue
            
        }
        
        visitEnd()
    }.toByteArray()
    
    return loadClass(qualifiedName, bytes, classLoader)?.newInstance() as? Annotation
}

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