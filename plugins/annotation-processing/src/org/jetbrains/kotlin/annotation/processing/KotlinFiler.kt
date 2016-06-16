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

import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.tools.FileObject
import javax.tools.JavaFileManager
import javax.tools.JavaFileObject

class KotlinFiler : Filer {
    override fun createSourceFile(name: CharSequence, vararg originatingElements: Element?): JavaFileObject? {
        throw UnsupportedOperationException()
    }

    override fun getResource(location: JavaFileManager.Location, pkg: CharSequence, relativeName: CharSequence): FileObject? {
        throw UnsupportedOperationException()
    }

    override fun createResource(
            location: JavaFileManager.Location?, 
            pkg: CharSequence, 
            relativeName: CharSequence, 
            vararg originatingElements: Element?
    ): FileObject? {
        throw UnsupportedOperationException()
    }

    override fun createClassFile(name: CharSequence, vararg originatingElements: Element?): JavaFileObject? {
        throw UnsupportedOperationException()
    }
}