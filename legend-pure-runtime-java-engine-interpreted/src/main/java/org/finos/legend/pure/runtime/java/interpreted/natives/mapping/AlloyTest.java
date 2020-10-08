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

package org.finos.legend.pure.runtime.java.interpreted.natives.mapping;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.pure.m3.navigation.M3Properties;
import org.finos.legend.pure.m3.exception.PureExecutionException;
import org.finos.legend.pure.m3.compiler.Context;
import org.finos.legend.pure.m3.navigation.Instance;
import org.finos.legend.pure.m3.navigation.ValueSpecificationBootstrap;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.FunctionCoreInstanceWrapper;
import org.finos.legend.pure.m3.navigation.ProcessorSupport;
import org.finos.legend.pure.m4.coreinstance.CoreInstance;
import org.finos.legend.pure.m4.ModelRepository;
import org.finos.legend.pure.runtime.java.interpreted.ExecutionSupport;
import org.finos.legend.pure.runtime.java.interpreted.FunctionExecutionInterpreted;
import org.finos.legend.pure.runtime.java.interpreted.VariableContext;
import org.finos.legend.pure.runtime.java.interpreted.natives.core.InstantiationContext;
import org.finos.legend.pure.runtime.java.interpreted.natives.core.NativeFunction;
import org.finos.legend.pure.runtime.java.interpreted.profiler.Profiler;

import java.util.Stack;

public class AlloyTest extends NativeFunction
{
    private final FunctionExecutionInterpreted functionExecution;
    private final ModelRepository repository;
    private final boolean useClientVersion;
    private final boolean useServerVersion;

    public AlloyTest(ModelRepository repository, FunctionExecutionInterpreted functionExecution, boolean useClientVersion, boolean useServerVersion)
    {
        this.functionExecution = functionExecution;
        this.repository = repository;
        this.useClientVersion = useClientVersion;
        this.useServerVersion = useServerVersion;
    }

    @Override
    public CoreInstance execute(ListIterable<? extends CoreInstance> params, Stack<MutableMap<String, CoreInstance>> resolvedTypeParameters, Stack<MutableMap<String, CoreInstance>> resolvedMultiplicityParameters, VariableContext variableContext, CoreInstance functionExpressionToUseInStack, Profiler profiler, InstantiationContext instantiationContext, ExecutionSupport executionSupport, Context context, final ProcessorSupport processorSupport) throws PureExecutionException
    {
        String host = System.getProperty("alloy.test.server.host");
        String clientVersion = System.getProperty("alloy.test.version");
        if (clientVersion == null)
        {
            clientVersion = System.getProperty("alloy.test.clientVersion");
        }
        String serverVersion = System.getProperty("alloy.test.serverVersion");
        int port = System.getProperty("alloy.test.server.port") == null ? -1 : Integer.parseInt(System.getProperty("alloy.test.server.port"));

        if (host != null && port == -1)
        {
            throw new PureExecutionException(functionExpressionToUseInStack.getSourceInformation(), "The system variable 'alloy.test.server.host' is set to '"+host+"' however 'alloy.test.server.port' has not been set!");
        }
        if (host != null)
        {
            MutableList<CoreInstance> fParams = FastList.<CoreInstance>newListWith(
                    ValueSpecificationBootstrap.newStringLiteral(this.repository, host, this.functionExecution.getProcessorSupport()),
                    ValueSpecificationBootstrap.newIntegerLiteral(this.repository, port, this.functionExecution.getProcessorSupport()));

            if (this.useClientVersion)
            {
                fParams.add(0, ValueSpecificationBootstrap.newStringLiteral(this.repository, clientVersion, this.functionExecution.getProcessorSupport()));
            }

            if (this.useServerVersion)
            {
                fParams.add(1, ValueSpecificationBootstrap.newStringLiteral(this.repository, serverVersion, this.functionExecution.getProcessorSupport()));
            }

            return this.functionExecution.executeFunctionExecuteParams(FunctionCoreInstanceWrapper.toFunction(Instance.getValueForMetaPropertyToOneResolved(params.get(0), M3Properties.values, processorSupport)),
                    fParams,
                    resolvedTypeParameters,
                    resolvedMultiplicityParameters,
                    getParentOrEmptyVariableContext(variableContext),
                    functionExpressionToUseInStack,
                    profiler,
                    instantiationContext,
                    executionSupport);
        }
        else
        {
            return this.functionExecution.executeFunctionExecuteParams(FunctionCoreInstanceWrapper.toFunction(Instance.getValueForMetaPropertyToOneResolved(params.get(1), M3Properties.values, processorSupport)),
                    FastList.<CoreInstance>newList(),
                    resolvedTypeParameters,
                    resolvedMultiplicityParameters,
                    getParentOrEmptyVariableContext(variableContext),
                    functionExpressionToUseInStack,
                    profiler,
                    instantiationContext,
                    executionSupport);

        }
    }

}
