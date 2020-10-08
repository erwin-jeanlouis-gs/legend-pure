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

package org.finos.legend.pure.runtime.java.interpreted.natives.core.string;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.finos.legend.pure.m3.navigation.M3Properties;
import org.finos.legend.pure.m3.compiler.Context;
import org.finos.legend.pure.m3.navigation.Instance;
import org.finos.legend.pure.m3.navigation.ValueSpecificationBootstrap;
import org.finos.legend.pure.m3.navigation.ProcessorSupport;
import org.finos.legend.pure.m4.coreinstance.CoreInstance;
import org.finos.legend.pure.m4.ModelRepository;
import org.finos.legend.pure.runtime.java.interpreted.ExecutionSupport;
import org.finos.legend.pure.runtime.java.interpreted.VariableContext;
import org.finos.legend.pure.runtime.java.interpreted.natives.core.NativeFunction;
import org.finos.legend.pure.runtime.java.interpreted.natives.core.InstantiationContext;
import org.finos.legend.pure.runtime.java.interpreted.profiler.Profiler;

import java.util.Stack;

public class JoinStrings extends NativeFunction
{
    private final ModelRepository repository;

    public JoinStrings(ModelRepository repository)
    {
        this.repository = repository;
    }

    @Override
    public CoreInstance execute(ListIterable<? extends CoreInstance> params, Stack<MutableMap<String, CoreInstance>> resolvedTypeParameters, Stack<MutableMap<String, CoreInstance>> resolvedMultiplicityParameters, VariableContext variableContext, CoreInstance functionExpressionToUseInStack, Profiler profiler, InstantiationContext instantiationContext, ExecutionSupport executionSupport, Context context, ProcessorSupport processorSupport)
    {
        ListIterable<? extends CoreInstance> strings = Instance.getValueForMetaPropertyToManyResolved(params.get(0), M3Properties.values, processorSupport);
        String prefix = Instance.getValueForMetaPropertyToOneResolved(params.get(1), M3Properties.values, processorSupport).getName();
        String suffix = Instance.getValueForMetaPropertyToOneResolved(params.get(3), M3Properties.values, processorSupport).getName();

        int stringCount = strings.size();
        switch (stringCount)
        {
            case 0:
            {
                if (prefix.isEmpty())
                {
                    // Return suffix value specification
                    return params.get(3);
                }
                else if (suffix.isEmpty())
                {
                    // Return prefix value specification
                    return params.get(1);
                }
                else
                {
                    return ValueSpecificationBootstrap.newStringLiteral(this.repository, prefix + suffix, processorSupport);
                }
            }
            case 1:
            {
                if (prefix.isEmpty() && suffix.isEmpty())
                {
                    return ValueSpecificationBootstrap.wrapValueSpecification(strings.get(0), true, processorSupport);
                }
                else
                {
                    return ValueSpecificationBootstrap.newStringLiteral(this.repository, prefix + strings.get(0).getName() + suffix, processorSupport);
                }

            }
            default:
            {
                String separator = Instance.getValueForMetaPropertyToOneResolved(params.get(2), M3Properties.values, processorSupport).getName();

                StringBuilder builder = new StringBuilder(prefix.length() + suffix.length() + (stringCount * (8 + separator.length())));
                builder.append(prefix);
                if (separator.isEmpty())
                {
                    for (int i = 0; i < stringCount; i++)
                    {
                        builder.append(strings.get(i).getName());
                    }
                }
                else
                {
                    builder.append(strings.get(0).getName());
                    for (int i = 1; i < stringCount; i++)
                    {
                        builder.append(separator);
                        builder.append(strings.get(i).getName());
                    }
                }
                builder.append(suffix);

                return ValueSpecificationBootstrap.newStringLiteral(this.repository, builder.toString(), processorSupport);
            }
        }
    }
}
