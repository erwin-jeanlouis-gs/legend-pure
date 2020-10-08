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

package org.finos.legend.pure.runtime.java.compiled.generation.processors.support.function.base.lang;

import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.test.Verify;
import org.finos.legend.pure.m3.AbstractPureTestWithCoreCompiled;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.InstanceValue;
import org.finos.legend.pure.m3.execution.FunctionExecution;
import org.finos.legend.pure.m4.coreinstance.CoreInstance;
import org.finos.legend.pure.runtime.java.compiled.execution.FunctionExecutionCompiledBuilder;
import org.junit.Assert;
import org.junit.Test;

public class TestIfCompiled extends AbstractPureTestWithCoreCompiled
{
    @Test
    public void testUnAssignedIfInFuncExpression()
    {
        try
        {
            String func = "function meta::pure::functions::lang::tests::if::testUnAssignedIfInFuncExpression():String[1]\n" +
                    "{\n" +
                    "   let ifVar =  if(true, | let b = 'true', | if(true, | let b = 'true', | 'false'););\n" +
                    "   if(true, | let b = 'true', | if(true, | let b = 'true', | 'false'););\n" +
                    "   if(true, | let b = 'true', | if(true, | let b = 'true', | 'false'););\n" +
                    "   let iff = if(true, | let b = 'true', | if(true, | let b = 'true', | 'false'););\n" +
                    "   if(true, | let b = 'true'; if(true, | let b = 'true', | 'false'); let bb = 'bb';, | let c = 'see');\n" +
                    "   let a = 'be';\n" +
                    "   if(true, | let b = 'true', | 'false');\n" +
                    "}";
            this.compileTestSource(func);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testIfWithDifferentMultiplicities()
    {
        try
        {
            compileTestSource("Class A\n" +
                    "{\n" +
                    "  id : Integer[1];\n" +
                    "}\n" +
                    "\n" +
                    "function testFn(ids:Integer[*]):A[*]\n" +
                    "{\n" +
                    "  if ($ids->isEmpty(),\n" +
                    "      | let id = -1;\n" +
                    "        ^A(id=$id);,\n" +
                    "      | let newIds = $ids->tail();\n" +
                    "        $ids->map(id | $newIds->testFn());)\n" +
                    "}");
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testIfWithFloatVersusInteger()
    {
        compileTestSource("import test::*;\n" +
                "function test::testFn(test:Boolean[1]):Number[1]\n" +
                "{\n" +
                "    let result = if($test, |1.0, |3);\n" +
                "    $result;\n" +
                "}\n" +
                "\n" +
                "function test::testTrue():Any[*]\n" +
                "{\n" +
                "  let result = testFn(true);\n" +
                "  assert(1.0 == $result, |'');\n" +
                "  $result;\n" +
                "}\n" +
                "\n" +
                "function test::testFalse():Any[*]\n" +
                "{\n" +
                "  let result = testFn(false);\n" +
                "  assert(3 == $result, |'');\n" +
                "  $result;\n" +
                "}\n");

        CoreInstance testTrue = this.runtime.getFunction("test::testTrue():Any[*]");
        Assert.assertNotNull(testTrue);
        CoreInstance resultTrue = this.functionExecution.start(testTrue, Lists.immutable.<CoreInstance>empty());
        Verify.assertInstanceOf(InstanceValue.class, resultTrue);
        InstanceValue trueInstanceValue = (InstanceValue)resultTrue;
        Verify.assertSize(1, trueInstanceValue._values());
        Object trueValue = trueInstanceValue._values().getFirst();
        Verify.assertInstanceOf(Double.class, trueValue);
        Assert.assertEquals(1.0d, trueValue);

        CoreInstance testFalse = this.runtime.getFunction("test::testFalse():Any[*]");
        Assert.assertNotNull(testFalse);
        CoreInstance resultFalse = this.functionExecution.start(testFalse, Lists.immutable.<CoreInstance>empty());
        Verify.assertInstanceOf(InstanceValue.class, resultFalse);
        InstanceValue falseInstanceValue = (InstanceValue)resultFalse;
        Verify.assertSize(1, falseInstanceValue._values());
        Object falseValue = falseInstanceValue._values().getFirst();
        Verify.assertInstanceOf(Long.class, falseValue);
        Assert.assertEquals(3L, falseValue);
    }

    @Override
    public FunctionExecution getFunctionExecution()
    {
        return new FunctionExecutionCompiledBuilder().build();
    }
}
