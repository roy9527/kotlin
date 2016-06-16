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

package org.jetbrains.kotlin.annotation.processing.impl

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.TypeConversionUtil
import org.jetbrains.kotlin.java.model.impl.*
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.*
import javax.lang.model.util.Types

class KotlinTypes(val javaPsiFacade: JavaPsiFacade, val scope: GlobalSearchScope) : Types {
    override fun contains(t1: TypeMirror, t2: TypeMirror): Boolean {
        t1 as? JeAbstractType ?: return false
        t2 as? JeAbstractType ?: return false
        
        val classType = t1.psi as? PsiClassType ?: return false
        return t2.psi in classType.parameters
    }

    override fun getArrayType(componentType: TypeMirror) = JeArrayTypeWithComponent(componentType)

    override fun isAssignable(t1: TypeMirror, t2: TypeMirror): Boolean {
        fun error(t: TypeMirror?): Nothing = throw IllegalArgumentException("Invalid type: $t")
        if (t1 is ExecutableType || t1 is NoType) error(t1)
        if (t2 is ExecutableType || t2 is NoType) error(t2)
        
        t1 as? JeAbstractType ?: return false
        t2 as? JeAbstractType ?: return false
        return t1.psi.isAssignableFrom(t2.psi)
    }

    override fun getNullType() = JeNullType

    override fun getWildcardType(extendsBound: TypeMirror, superBound: TypeMirror) = JeWildcardTypeWithBounds(extendsBound, superBound)

    override fun unboxedType(t: TypeMirror): PrimitiveType? {
        fun error(): Nothing = throw IllegalArgumentException("This type could not be unboxed: $t")
        t as? JeAbstractType ?: error()
        val unboxedType = PsiPrimitiveType.getUnboxedType(t.psi) ?: error()
        return getJePrimitiveType(unboxedType)
    }

    override fun getPrimitiveType(kind: TypeKind) = getJePrimitiveType(kind)

    override fun erasure(t: TypeMirror): TypeMirror {
        if (t is NoType) throw IllegalArgumentException("Invalid type: $t")
        t as? JeAbstractType ?: return t
        return JeTypeMirror(TypeConversionUtil.erasure(t.psi))
    }

    override fun directSupertypes(t: TypeMirror): List<TypeMirror> {
        if (t is NoType) throw IllegalArgumentException("Invalid type: $t")
        t as? JeDeclaredType ?: return emptyList()
        val clazz = PsiTypesUtil.getPsiClass(t.psi) ?: return emptyList()
        
        val superTypes = mutableListOf<TypeMirror>()
        clazz.superClass?.let { superTypes += JeTypeMirror(PsiTypesUtil.getClassType(it)) }
        clazz.interfaces.forEach { superTypes += JeTypeMirror(PsiTypesUtil.getClassType(it)) }
        return superTypes
    }

    override fun boxedClass(p: PrimitiveType): TypeElement? {
        p as? JePrimitiveType ?: throw IllegalArgumentException("Unknown type: $p")
        val boxedTypeName = p.psi.boxedTypeName
        val boxedClass = javaPsiFacade.findClass(boxedTypeName, scope) 
                         ?: throw IllegalStateException("Can't find boxed class $boxedTypeName")
        return JeTypeElement(boxedClass)
    }

    override fun getDeclaredType(typeElem: TypeElement, vararg typeArgs: TypeMirror) = JeCompoundDeclaredType(typeElem, typeArgs.toList())

    override fun getDeclaredType(containing: DeclaredType?, typeElem: TypeElement, vararg typeArgs: TypeMirror): DeclaredType? {
        TODO()
    }

    override fun asMemberOf(containing: DeclaredType, element: Element) = TODO()

    override fun isSameType(t1: TypeMirror, t2: TypeMirror): Boolean {
        if (t1 === t2) return true
        t1 as? JeAbstractType ?: return false
        t2 as? JeAbstractType ?: return false
        return t1.psi == t2.psi
    }

    override fun getNoType(kind: TypeKind) = CustomJeNoneType(kind)

    override fun isSubsignature(m1: ExecutableType, m2: ExecutableType): Boolean {
        TODO()
    }

    override fun capture(t: TypeMirror): TypeMirror? {
        TODO()
    }

    override fun asElement(t: TypeMirror): Element? {
        if (t is JeDeclaredType) {
            return t.asElement()
        }
        
        return null
    }

    override fun isSubtype(t1: TypeMirror, t2: TypeMirror): Boolean {
        t1 as? JeAbstractType ?: return false
        t2 as? JeAbstractType ?: return false
        return t2.psi in t1.psi.superTypes
    }
}