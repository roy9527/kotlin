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

package org.jetbrains.kotlin.java.model.impl

import com.intellij.psi.PsiAnnotationOwner
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.util.ClassUtil
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.kotlin.java.model.JeAnnotationOwner
import org.jetbrains.kotlin.java.model.JeElement
import org.jetbrains.kotlin.java.model.JeModifierListOwner
import org.jetbrains.kotlin.java.model.JeName
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

class JeTypeElement(override val psi: PsiClass) : JeElement(), TypeElement, JeAnnotationOwner, JeModifierListOwner {
    override val annotationOwner: PsiAnnotationOwner?
        get() = psi.modifierList

    override fun getSimpleName() = JeName(psi.name)

    override fun getQualifiedName() = JeName(psi.qualifiedName)

    override fun getSuperclass(): TypeMirror? {
        val superClass = psi.superClass ?: return JeNoneType
        return JeTypeMirror(PsiTypesUtil.getClassType(superClass))
    }

    override fun getInterfaces() = psi.interfaces.map { JeTypeMirror(PsiTypesUtil.getClassType(it)) }

    override fun getTypeParameters() = psi.typeParameters.map { JeTypeParameterElement(it, this) }

    override fun getNestingKind() = when {
        ClassUtil.isTopLevelClass(psi) -> NestingKind.TOP_LEVEL
        psi.parent is PsiClass -> NestingKind.MEMBER
        psi is PsiAnonymousClass -> NestingKind.ANONYMOUS
        else -> NestingKind.LOCAL
    }

    override fun getEnclosedElements(): MutableList<out Element>? {
        val declarations = mutableListOf<Element>()
        psi.initializers.forEach { declarations += JeClassInitializerExecutableElement(it) }
        psi.constructors.forEach { declarations += JeExecutableElement(it) }
        psi.fields.forEach { declarations += JeVariableElement(it) }
        psi.methods.forEach { declarations += JeExecutableElement(it) }
        psi.innerClasses.forEach { declarations += JeTypeElement(it) }
        return declarations
    }

    override fun getKind() = when {
        psi.isInterface -> ElementKind.INTERFACE
        psi.isEnum -> ElementKind.ENUM
        psi.isAnnotationType -> ElementKind.ANNOTATION_TYPE
        else -> ElementKind.CLASS
    }

    override fun asType() = JeTypeMirror(PsiTypesUtil.getClassType(psi))

    override fun <R : Any?, P : Any?> accept(v: ElementVisitor<R, P>, p: P) = v.visitType(this, p)
}