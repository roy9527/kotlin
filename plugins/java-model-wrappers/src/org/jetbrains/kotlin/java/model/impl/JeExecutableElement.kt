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

import com.intellij.psi.PsiAnnotationMethod
import com.intellij.psi.PsiAnnotationOwner
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.kotlin.java.model.*
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

class JeExecutableElement(override val psi: PsiMethod) : JeElement(), ExecutableElement, JeModifierListOwner, JeAnnotationOwner {
    override fun getEnclosingElement() = psi.containingClass?.let { JeConverter.convertClass(it) }

    override fun getSimpleName(): JeName {
        if (psi.isConstructor) return JeName.INIT
        return JeName(psi.name)
    }

    override fun getThrownTypes() = psi.throwsList.referencedTypes.map { JeTypeMirror(it) }

    override fun getTypeParameters() = psi.typeParameters.map { JeTypeParameterElement(it, this) }

    override fun getParameters() = psi.parameterList.parameters.map { JeVariableElement(it) }

    override fun getDefaultValue(): AnnotationValue? {
        val annotationMethod = psi as? PsiAnnotationMethod ?: return null
        val defaultValue = annotationMethod.defaultValue ?: return null
        return JeAnnotationValue(defaultValue)
    }

    override fun getReturnType() = psi.returnType?.let { JeTypeMirror(it) } ?: JeNoneType

    override fun getReceiverType() = psi.getReceiverTypeMirror()
    
    override fun isVarArgs() = psi.isVarArgs

    override fun isDefault() = psi.hasModifierProperty(PsiModifier.DEFAULT)

    override fun getKind() = when {
        psi.isConstructor -> ElementKind.CONSTRUCTOR
        else -> ElementKind.METHOD
    }

    override fun asType() = JeExecutableTypeMirror(psi)

    override fun <R : Any?, P : Any?> accept(v: ElementVisitor<R, P>, p: P) = v.visitExecutable(this, p)

    override fun getEnclosedElements() = emptyList<Element>()

    override val annotationOwner: PsiAnnotationOwner?
        get() = psi.modifierList
}

fun PsiMethod.getReceiverTypeMirror(): TypeMirror {
    if (hasModifierProperty(PsiModifier.STATIC)) return JeNoneType

    if (isConstructor) {
        val containingClass = containingClass
        if (containingClass != null) {
            containingClass.containingClass?.let {
                return JeTypeMirror(PsiTypesUtil.getClassType(it))
            }
        }

        return JeNoneType
    }

    val containingClass = containingClass ?: return JeNoneType
    return JeTypeMirror(PsiTypesUtil.getClassType(containingClass))

}