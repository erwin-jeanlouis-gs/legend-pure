// Copyright 2020 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.pure.runtime.java.interpreted.natives.grammar.collection.iteration;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.stack.MutableStack;
import org.finos.legend.pure.m3.compiler.Context;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.Function;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.FunctionCoreInstanceWrapper;
import org.finos.legend.pure.m3.exception.PureExecutionException;
import org.finos.legend.pure.m3.navigation.Instance;
import org.finos.legend.pure.m3.navigation.M3Properties;
import org.finos.legend.pure.m3.navigation.ProcessorSupport;
import org.finos.legend.pure.m3.navigation.ValueSpecificationBootstrap;
import org.finos.legend.pure.m3.navigation.valuespecification.ValueSpecification;
import org.finos.legend.pure.m4.coreinstance.CoreInstance;
import org.finos.legend.pure.runtime.java.interpreted.ExecutionSupport;
import org.finos.legend.pure.runtime.java.interpreted.FunctionExecutionInterpreted;
import org.finos.legend.pure.runtime.java.interpreted.VariableContext;
import org.finos.legend.pure.runtime.java.interpreted.natives.InstantiationContext;
import org.finos.legend.pure.runtime.java.interpreted.natives.NativeFunction;
import org.finos.legend.pure.runtime.java.interpreted.profiler.Profiler;

import java.util.Stack;

public class Map extends NativeFunction
{
    private final FunctionExecutionInterpreted functionExecution;

    public Map(FunctionExecutionInterpreted functionExecution)
    {
        this.functionExecution = functionExecution;
    }

    @Override
    public CoreInstance execute(ListIterable<? extends CoreInstance> params, Stack<MutableMap<String, CoreInstance>> resolvedTypeParameters, Stack<MutableMap<String, CoreInstance>> resolvedMultiplicityParameters, VariableContext variableContext, MutableStack<CoreInstance> functionExpressionCallStack, Profiler profiler, InstantiationContext instantiationContext, ExecutionSupport executionSupport, Context context, ProcessorSupport processorSupport) throws PureExecutionException
    {
        ListIterable<? extends CoreInstance> collection = Instance.getValueForMetaPropertyToManyResolved(params.get(0), M3Properties.values, processorSupport);
        boolean isExecutable = ValueSpecification.isExecutable(params.get(0), processorSupport);
        if (collection.isEmpty())
        {
            return ValueSpecificationBootstrap.wrapValueSpecification(Lists.immutable.empty(), isExecutable, processorSupport);
        }

        CoreInstance collectFunction = Instance.getValueForMetaPropertyToOneResolved(params.get(1), M3Properties.values, processorSupport);
        Function<?> function = FunctionCoreInstanceWrapper.toFunction(collectFunction);
        VariableContext evalVarContext = getParentOrEmptyVariableContextForLambda(variableContext, collectFunction);
        MutableList<CoreInstance> results = Lists.mutable.with();
        collection.forEach(instance ->
        {
            ListIterable<CoreInstance> parameters = Lists.immutable.with(ValueSpecificationBootstrap.wrapValueSpecification(instance, isExecutable, processorSupport));
            CoreInstance subResult = this.functionExecution.executeFunction(false, function, parameters, resolvedTypeParameters, resolvedMultiplicityParameters, evalVarContext, functionExpressionCallStack, profiler, instantiationContext, executionSupport);
            results.addAllIterable(Instance.getValueForMetaPropertyToManyResolved(subResult, M3Properties.values, processorSupport));
        });
        return ValueSpecificationBootstrap.wrapValueSpecification_ForFunctionReturnValue(Instance.getValueForMetaPropertyToOneResolved(functionExpressionCallStack.peek(), M3Properties.genericType, processorSupport), results, isExecutable, processorSupport);
    }
}
