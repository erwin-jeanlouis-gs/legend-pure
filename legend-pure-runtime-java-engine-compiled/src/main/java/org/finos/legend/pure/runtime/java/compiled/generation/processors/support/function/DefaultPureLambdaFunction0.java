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

package org.finos.legend.pure.runtime.java.compiled.generation.processors.support.function;

import org.eclipse.collections.api.map.MutableMap;
import org.finos.legend.pure.m3.execution.ExecutionSupport;

/**
 * Used when there are no open variables
 */
public abstract class DefaultPureLambdaFunction0<T> implements PureLambdaFunction0<T>
{
    @Override
    public MutableMap<String, Object> getOpenVariables()
    {
        return null;
    }

    @Override
    public T apply(ExecutionSupport each)
    {
        return this.valueOf(each);
    }
}
