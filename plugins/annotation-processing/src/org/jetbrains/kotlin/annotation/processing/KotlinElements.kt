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
import com.intellij.psi.search.GlobalSearchScope
import com.sun.tools.javac.util.Constants
import org.jetbrains.kotlin.java.model.*
import org.jetbrains.kotlin.java.model.impl.JeAnnotationMirror
import org.jetbrains.kotlin.java.model.impl.JeExecutableElement
import org.jetbrains.kotlin.java.model.impl.JeTypeElement
import java.io.PrintWriter
import java.io.Writer
import javax.lang.model.element.*
import javax.lang.model.util.Elements

class KotlinElements(val javaPsiFacade: JavaPsiFacade, val scope: GlobalSearchScope) : Elements {
    override fun hides(hider: Element?, hidden: Element?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun overrides(overrider: ExecutableElement?, overridden: ExecutableElement?, type: TypeElement?): Boolean {
        val jeOverrider = overrider as? JeExecutableElement ?: return false
        val jeOverridden = overridden as? JeExecutableElement ?: return false
        
        
    }

    override fun getName(cs: CharSequence?) = JeName(cs?.toString())

    override fun getElementValuesWithDefaults(a: AnnotationMirror?): Map<out ExecutableElement, AnnotationValue> {
        val jeAnnotation = a as? JeAnnotationMirror ?: return emptyMap()
        return jeAnnotation.getAllElementValues()
    }

    override fun getBinaryName(type: TypeElement?) = JeName((type as JeTypeElement).psi.qualifiedName)

    override fun getDocComment(e: Element?) = ""

    override fun isDeprecated(e: Element?): Boolean {
        return (e as? JeAnnotationOwner)?.annotationOwner?.findAnnotation("java.lang.Deprecated") != null
    }

    override fun getAllMembers(type: TypeElement?): List<Element> {
        val jeTypeElement = type as? JeTypeElement ?: return emptyList()
        return jeTypeElement.getAllMembers();
    }

    override fun printElements(w: Writer, vararg elements: Element) {
        val printWriter = PrintWriter(w)
        for (element in elements) {
            printWriter.println(element.simpleName.toString() + " (" + element.javaClass.name + ")")
        }
    }

    override fun getPackageElement(name: CharSequence): PackageElement? {
        val psiPackage = javaPsiFacade.findPackage(name.toString()) ?: return null
        return JeConverter.convertPackage(psiPackage)
    }

    override fun getTypeElement(name: CharSequence): TypeElement? {
        val psiClass = javaPsiFacade.findClass(name.toString(), scope) ?: return null
        return JeConverter.convertClass(psiClass)
    }

    override fun getConstantExpression(value: Any?) = Constants.format(value)

    override tailrec fun getPackageOf(element: Element): PackageElement? {
        if (element is PackageElement) return element
        val parent = element.enclosingElement ?: return null
        return getPackageOf(parent)
    }

    override fun getAllAnnotationMirrors(e: Element?): List<AnnotationMirror> {
        throw UnsupportedOperationException()
    }
}