/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.k2js.translate.expression;


import com.google.dart.compiler.backend.js.ast.*;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.descriptors.FunctionDescriptor;
import org.jetbrains.jet.lang.descriptors.Modality;
import org.jetbrains.jet.lang.descriptors.ValueParameterDescriptor;
import org.jetbrains.jet.lang.psi.JetDeclarationWithBody;
import org.jetbrains.jet.lang.psi.JetExpression;
import org.jetbrains.jet.lang.psi.JetFunctionLiteralExpression;
import org.jetbrains.k2js.translate.context.AliasingContext;
import org.jetbrains.k2js.translate.context.Namer;
import org.jetbrains.k2js.translate.context.TranslationContext;
import org.jetbrains.k2js.translate.general.AbstractTranslator;
import org.jetbrains.k2js.translate.utils.TranslationUtils;

import java.util.Collections;
import java.util.List;

import static org.jetbrains.k2js.translate.utils.ErrorReportingUtils.message;
import static org.jetbrains.k2js.translate.utils.FunctionBodyTranslator.translateFunctionBody;
import static org.jetbrains.k2js.translate.utils.JsAstUtils.setParameters;
import static org.jetbrains.k2js.translate.utils.JsDescriptorUtils.getExpectedReceiverDescriptor;

/**
 * @author Pavel Talanov
 */
public final class FunctionTranslator extends AbstractTranslator {
    @NotNull
    private final TranslationContext functionBodyContext;
    @NotNull
    private final JetDeclarationWithBody functionDeclaration;
    @Nullable
    private JsName extensionFunctionReceiverName;
    @NotNull
    private final JsFunction functionObject;
    @NotNull
    private final FunctionDescriptor descriptor;

    public FunctionTranslator(@NotNull JetDeclarationWithBody functionDeclaration, @NotNull FunctionDescriptor functionDescriptor,  @NotNull TranslationContext context) {
        super(context);
        this.descriptor = functionDescriptor;
        this.functionDeclaration = functionDeclaration;
        functionObject = new JsFunction(context.scope(), new JsBlock());
        assert functionObject.getParameters().isEmpty()
                : message(bindingContext(), descriptor, "Function " + functionDeclaration.getText() + " processed for the second time.");
        //NOTE: it's important we compute the context before we start the computation
        functionBodyContext = getFunctionBodyContext();
    }

    @NotNull
    private TranslationContext getFunctionBodyContext() {
        AliasingContext aliasingContext;
        if (isExtensionFunction()) {
            DeclarationDescriptor expectedReceiverDescriptor = getExpectedReceiverDescriptor(descriptor);
            assert expectedReceiverDescriptor != null;
            extensionFunctionReceiverName = functionObject.getScope().declareName(Namer.getReceiverParameterName());
            //noinspection ConstantConditions
            aliasingContext = context().aliasingContext().inner(expectedReceiverDescriptor, extensionFunctionReceiverName.makeRef());
        }
        else {
            aliasingContext = null;
        }
        return context().newFunctionBody(functionObject, aliasingContext, null);
    }

    @NotNull
    public JsPropertyInitializer translateAsEcma5PropertyDescriptor() {
        generateFunctionObject();
        return TranslationUtils.translateFunctionAsEcma5PropertyDescriptor(functionObject, descriptor, context());
    }

    @NotNull
    public JsPropertyInitializer translateAsMethod() {
        generateFunctionObject();
        return new JsPropertyInitializer(context().getNameRefForDescriptor(descriptor), functionObject);
    }

    private void generateFunctionObject() {
        setParameters(functionObject, translateParameters());
        JetExpression jetBodyExpression = functionDeclaration.getBodyExpression();
        if (jetBodyExpression == null) {
            assert descriptor.getModality().equals(Modality.ABSTRACT);
            return;
        }
        functionObject.getBody().getStatements().addAll(translateFunctionBody(descriptor, functionDeclaration, functionBodyContext).getStatements());
    }

    @NotNull
    private List<JsParameter> translateParameters() {
        if (extensionFunctionReceiverName == null && descriptor.getValueParameters().isEmpty()) {
            return Collections.emptyList();
        }

        List<JsParameter> jsParameters = new SmartList<JsParameter>();
        mayBeAddThisParameterForExtensionFunction(jsParameters);
        addParameters(jsParameters, descriptor, context());
        return jsParameters;
    }

    public static void addParameters(List<JsParameter> list, FunctionDescriptor descriptor, TranslationContext context) {
        for (ValueParameterDescriptor valueParameter : descriptor.getValueParameters()) {
            list.add(new JsParameter(context.getNameForDescriptor(valueParameter)));
        }
    }

    private void mayBeAddThisParameterForExtensionFunction(@NotNull List<JsParameter> jsParameters) {
        if (isExtensionFunction()) {
            assert extensionFunctionReceiverName != null;
            jsParameters.add(new JsParameter(extensionFunctionReceiverName));
        }
    }

    private boolean isExtensionFunction() {
        return descriptor.getReceiverParameter() != null && !(functionDeclaration instanceof JetFunctionLiteralExpression);
    }
}
