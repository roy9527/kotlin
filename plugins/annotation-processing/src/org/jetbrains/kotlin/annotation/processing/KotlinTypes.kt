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

package org.jetbrains.kotlin.annotation.processing

import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.*
import javax.lang.model.util.Types

class KotlinTypes : Types {
    override fun contains(t1: TypeMirror?, t2: TypeMirror?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getArrayType(componentType: TypeMirror?): ArrayType? {
        throw UnsupportedOperationException()
    }

    override fun isAssignable(t1: TypeMirror?, t2: TypeMirror?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getNullType(): NullType? {
        throw UnsupportedOperationException()
    }

    override fun getWildcardType(extendsBound: TypeMirror?, superBound: TypeMirror?): WildcardType? {
        throw UnsupportedOperationException()
    }

    override fun unboxedType(t: TypeMirror?): PrimitiveType? {
        throw UnsupportedOperationException()
    }

    override fun getPrimitiveType(kind: TypeKind?): PrimitiveType? {
        throw UnsupportedOperationException()
    }

    override fun erasure(t: TypeMirror?): TypeMirror? {
        throw UnsupportedOperationException()
    }

    override fun directSupertypes(t: TypeMirror?): MutableList<out TypeMirror>? {
        throw UnsupportedOperationException()
    }

    override fun boxedClass(p: PrimitiveType?): TypeElement? {
        throw UnsupportedOperationException()
    }

    override fun getDeclaredType(typeElem: TypeElement?, vararg typeArgs: TypeMirror?): DeclaredType? {
        throw UnsupportedOperationException()
    }

    override fun getDeclaredType(containing: DeclaredType?, typeElem: TypeElement?, vararg typeArgs: TypeMirror?): DeclaredType? {
        throw UnsupportedOperationException()
    }

    override fun asMemberOf(containing: DeclaredType?, element: Element?): TypeMirror? {
        throw UnsupportedOperationException()
    }

    override fun isSameType(t1: TypeMirror?, t2: TypeMirror?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getNoType(kind: TypeKind?): NoType? {
        throw UnsupportedOperationException()
    }

    override fun isSubsignature(m1: ExecutableType?, m2: ExecutableType?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun capture(t: TypeMirror?): TypeMirror? {
        throw UnsupportedOperationException()
    }

    override fun asElement(t: TypeMirror?): Element? {
        throw UnsupportedOperationException()
    }

    override fun isSubtype(t1: TypeMirror?, t2: TypeMirror?): Boolean {
        throw UnsupportedOperationException()
    }
}