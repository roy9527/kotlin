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

import com.intellij.psi.JavaPsiFacade
import org.jetbrains.kotlin.java.model.*
import java.io.Writer
import javax.lang.model.element.*
import javax.lang.model.util.Elements

class KotlinElements(val javaPsiFacade: JavaPsiFacade) : Elements {
    override fun hides(hider: Element?, hidden: Element?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun overrides(overrider: ExecutableElement?, overridden: ExecutableElement?, type: TypeElement?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getName(cs: CharSequence?) = JeName(cs?.toString())

    override fun getElementValuesWithDefaults(a: AnnotationMirror?): MutableMap<out ExecutableElement, out AnnotationValue>? {
        throw UnsupportedOperationException()
    }

    override fun getBinaryName(type: TypeElement?): Name? {
        throw UnsupportedOperationException()
    }

    override fun getDocComment(e: Element?): String? {
        throw UnsupportedOperationException()
    }

    override fun isDeprecated(e: Element?) = (e as? JeAnnotationOwner)?.annotationOwner?.findAnnotation("java.lang.Deprecated") != null

    override fun getAllMembers(type: TypeElement?): MutableList<out Element>? {
        throw UnsupportedOperationException()
    }

    override fun printElements(w: Writer?, vararg elements: Element?) {
        throw UnsupportedOperationException()
    }

    override fun getPackageElement(name: CharSequence?): PackageElement? {
        
        throw UnsupportedOperationException()
    }

    override fun getTypeElement(name: CharSequence): TypeElement? {
        
        throw UnsupportedOperationException()
    }

    override fun getConstantExpression(value: Any?): String? {
        throw UnsupportedOperationException()
    }

    override tailrec fun getPackageOf(element: Element): PackageElement? {
        if (element is PackageElement) return element
        val parent = element.enclosingElement ?: return null
        return getPackageOf(parent)
    }

    override fun getAllAnnotationMirrors(e: Element?): MutableList<out AnnotationMirror>? {
        throw UnsupportedOperationException()
    }
}