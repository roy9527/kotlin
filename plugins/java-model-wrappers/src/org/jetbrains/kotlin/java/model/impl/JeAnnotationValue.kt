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

import com.intellij.psi.PsiAnnotationMemberValue
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.AnnotationValueVisitor

class JeAnnotationValue(val psi: PsiAnnotationMemberValue) : AnnotationValue {
    override fun <R : Any?, P : Any?> accept(v: AnnotationValueVisitor<R, P>?, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun getValue(): Any? {
        throw UnsupportedOperationException()
    }
}