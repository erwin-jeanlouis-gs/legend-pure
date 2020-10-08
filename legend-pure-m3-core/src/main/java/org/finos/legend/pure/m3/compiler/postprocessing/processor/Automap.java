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

package org.finos.legend.pure.m3.compiler.postprocessing.processor;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.utility.Iterate;
import org.finos.legend.pure.m3.navigation.M3Properties;
import org.finos.legend.pure.m4.coreinstance.CoreInstance;

public class Automap {

    public static final String AUTOMAP_LAMBDA_VARIABLE_NAME = "v_automap";

    public static CoreInstance getAutoMapExpressionSequence(CoreInstance functionExpression){
        CoreInstance autoMapExpressionSequence = null;
        CoreInstance funcName = functionExpression.getValueForMetaPropertyToOne(M3Properties.functionName);
        if (funcName != null && "map".equals(funcName.getName())) {
            ListIterable<? extends CoreInstance> paramVals = functionExpression.getValueForMetaPropertyToMany(M3Properties.parametersValues);
            if (!Iterate.isEmpty(paramVals) && paramVals.size() == 2) {
                CoreInstance mapParam2 = paramVals.get(1);

                ListIterable<? extends CoreInstance> values = mapParam2.getValueForMetaPropertyToMany(M3Properties.values);

                if (!Iterate.isEmpty(values)) {
                    CoreInstance possibleLambda = values.get(0);
                    ListIterable<? extends CoreInstance> expressionSeq = possibleLambda.getValueForMetaPropertyToMany(M3Properties.expressionSequence);

                    if (!Iterate.isEmpty(expressionSeq)) {
                        CoreInstance possiblePropertySfe = expressionSeq.getFirst();

                        ListIterable<? extends CoreInstance> possiblePropertySfeParams = possiblePropertySfe.getValueForMetaPropertyToMany(M3Properties.parametersValues);
                        if (!Iterate.isEmpty(possiblePropertySfeParams)) {
                            CoreInstance possibleLambdaVarName = possiblePropertySfeParams.getFirst().getValueForMetaPropertyToOne(M3Properties.name);
                            if (!(possibleLambdaVarName == null) && AUTOMAP_LAMBDA_VARIABLE_NAME.equals(possibleLambdaVarName.getName())) {
                                return possiblePropertySfe;
                            }

                        }
                    }
                }
            }
        }
        return autoMapExpressionSequence;
    }
}
