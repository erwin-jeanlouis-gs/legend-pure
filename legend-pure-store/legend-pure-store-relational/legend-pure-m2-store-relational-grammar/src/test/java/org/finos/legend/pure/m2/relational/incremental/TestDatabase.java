// Copyright 2021 Goldman Sachs
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

package org.finos.legend.pure.m2.relational.incremental;

import org.finos.legend.pure.m2.relational.AbstractPureRelationalTestWithCoreCompiled;
import org.finos.legend.pure.m4.coreinstance.CoreInstance;
import org.finos.legend.pure.m4.exception.PureCompilationException;
import org.junit.Assert;
import org.junit.Test;

public class TestDatabase extends AbstractPureRelationalTestWithCoreCompiled
{
    @Test
    public void testDatabase() throws Exception
    {
        String relationalDB = "###Relational\n" +
                "Database db\n" +
                "(\n" +
                "   Table myTable\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   View myView\n" +
                "   (\n" +
                "       ~filter MyTableNameNotNullFilter" +
                "       myTableName : myTable.name\n" +
                "   )\n" +
                "Filter MyTableNameNotNullFilter(myTable.name is not null)" +
                ")";
        runtime.createInMemorySource("sourceId.pure", relationalDB);
        runtime.createInMemorySource("userId.pure", "function test():Boolean[1]{assert(1 == db->meta::relational::metamodel::schema('default').tables->size(), |'');}");

        runtime.compile();
        int size = runtime.getModelRepository().serialize().length;

        CoreInstance db = processorSupport.package_getByUserPath("db");
        Assert.assertEquals(1, db.getValueForMetaPropertyToMany("schemas").size());
        Assert.assertEquals(1, db.getValueForMetaPropertyToMany("schemas").getFirst().getValueForMetaPropertyToMany("tables").size());

        for (int i = 0; i < 10; i++)
        {
            runtime.delete("sourceId.pure");
            try
            {
                this.compileAndExecute("test():Boolean[1]");
                Assert.fail();
            }
            catch (Exception e)
            {
                Assert.assertEquals("Compilation error at (resource:userId.pure line:1 column:40), \"db has not been defined!\"", e.getMessage());
            }

            runtime.createInMemorySource("sourceId.pure", relationalDB);
            runtime.compile();
            Assert.assertEquals(size, runtime.getModelRepository().serialize().length);

            db = processorSupport.package_getByUserPath("db");
            Assert.assertEquals(1, db.getValueForMetaPropertyToMany("schemas").size());
            Assert.assertEquals(1, db.getValueForMetaPropertyToMany("schemas").getFirst().getValueForMetaPropertyToMany("tables").size());
        }
    }


    @Test
    public void testDatabaseError() throws Exception
    {
        String relationalDB = "###Relational\n" +
                "Database %s\n" +
                "(\n" +
                "   Table myTable\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   View myView\n" +
                "   (\n" +
                "       myTableName : myTable.name\n" +
                "   )\n" +
                ")";
        String relationalDB1 = String.format(relationalDB, "db");
        String relationalDB2 = String.format(relationalDB, "db2");
        runtime.createInMemorySource("sourceId.pure", relationalDB1);
        runtime.createInMemorySource("userId.pure", "function test():Boolean[1]{assert(1 == db->meta::relational::metamodel::schema('default').tables->size(), |'');}");

        runtime.compile();
        CoreInstance db = processorSupport.package_getByUserPath("db");
        Assert.assertEquals(1, db.getValueForMetaPropertyToMany("schemas").size());
        Assert.assertEquals(1, db.getValueForMetaPropertyToMany("schemas").getFirst().getValueForMetaPropertyToMany("tables").size());
        int size = runtime.getModelRepository().serialize().length;

        for (int i = 0; i < 10; i++)
        {
            runtime.delete("sourceId.pure");
            try
            {
                runtime.compile();
                Assert.fail();
            }
            catch (Exception e)
            {
                Assert.assertEquals("Compilation error at (resource:userId.pure line:1 column:40), \"db has not been defined!\"", e.getMessage());
            }

            try
            {
                runtime.createInMemorySource("sourceId.pure", relationalDB2);
                runtime.compile();
                Assert.fail();
            }
            catch (Exception e)
            {
                Assert.assertEquals("Compilation error at (resource:userId.pure line:1 column:40), \"db has not been defined!\"", e.getMessage());
            }

            runtime.delete("sourceId.pure");
            runtime.createInMemorySource("sourceId.pure", relationalDB1);
            runtime.compile();
            Assert.assertEquals(size, runtime.getModelRepository().serialize().length);
            db = processorSupport.package_getByUserPath("db");
            Assert.assertEquals(1, db.getValueForMetaPropertyToMany("schemas").size());
            Assert.assertEquals(1, db.getValueForMetaPropertyToMany("schemas").getFirst().getValueForMetaPropertyToMany("tables").size());
        }
    }


    @Test
    public void testDatabaseWithSchema() throws Exception
    {
        String relationalDB = "###Relational\n" +
                "Database db\n" +
                "(\n" +
                "   Schema mySchema1" +
                "   (" +
                "       Table myTable\n" +
                "       (\n" +
                "           name VARCHAR(200) PRIMARY KEY\n" +
                "       )" +
                "       View myView\n" +
                "       (\n" +
                "           myTableName : myTable.name\n" +
                "       )\n" +
                "   )" +
                "   Schema mySchema2" +
                "   (" +
                "       Table myTable2\n" +
                "       (\n" +
                "           name VARCHAR(200) PRIMARY KEY\n" +
                "       )" +
                "       View myView2\n" +
                "       (\n" +
                "           myTableName : myTable2.name\n" +
                "       )\n" +
                "   )" +
                ")\n";
        runtime.createInMemorySource("sourceId.pure", relationalDB);

        runtime.createInMemorySource("userId.pure", "function test():Boolean[1]{\n" +
                "     assert(1 == db->meta::relational::metamodel::schema('default'), |'');\n" +
                "     assert(1 == db->meta::relational::metamodel::schema('mySchema1').tables->size(), |'');\n" +
                "     assert(1 == db->meta::relational::metamodel::schema('mySchema2').tables->size(), |'');\n" +
                "}");

        runtime.compile();
        CoreInstance db = processorSupport.package_getByUserPath("db");
        Assert.assertEquals(2, db.getValueForMetaPropertyToMany("schemas").size());
        int size = runtime.getModelRepository().serialize().length;

        for (int i = 0; i < 10; i++)
        {
            runtime.delete("sourceId.pure");
            try
            {
                runtime.compile();
                Assert.fail();
            }
            catch (Exception e)
            {
                Assert.assertEquals("Compilation error at (resource:userId.pure line:2 column:18), \"db has not been defined!\"", e.getMessage());
            }

            runtime.createInMemorySource("sourceId.pure", relationalDB);
            runtime.compile();
            db = processorSupport.package_getByUserPath("db");
            Assert.assertEquals(2, db.getValueForMetaPropertyToMany("schemas").size());
            Assert.assertEquals(size, runtime.getModelRepository().serialize().length);
        }
    }

    @Test
    public void testDatabaseWithSchemaAndJoins() throws Exception
    {
        String relationalDB = "###Relational\n" +
                "Database db\n" +
                "(\n" +
                "   Schema mySchema1" +
                "   (" +
                "       Table myTable\n" +
                "       (\n" +
                "           name VARCHAR(200) PRIMARY KEY\n" +
                "       )" +
                "       View myView\n" +
                "       (\n" +
                "           myTableName : myTable.name\n" +
                "       )\n" +
                "   )" +
                "   Schema mySchema2" +
                "   (" +
                "       Table myTable2\n" +
                "       (\n" +
                "           name VARCHAR(200), id VARCHAR(200) PRIMARY KEY\n" +
                "       )" +
                "       View myView2\n" +
                "       (\n" +
                "           myTableName : myTable2.name\n" +
                "       )\n" +
                "   )" +
                "   Join testJoin(mySchema1.myTable.name = mySchema2.myTable2.id)" +
                ")\n";
        runtime.createInMemorySource("sourceId.pure", relationalDB);

        runtime.createInMemorySource("userId.pure", "function test():Boolean[1]{\n" +
                "     assert([] == db->meta::relational::metamodel::schema('default'), |'');\n" +
                "     assert(1 == db->meta::relational::metamodel::schema('mySchema1').tables->size(), |'');\n" +
                "     assert(1 == db->meta::relational::metamodel::schema('mySchema2').tables->size(), |'');\n" +
                "}");

        runtime.compile();

        CoreInstance db = processorSupport.package_getByUserPath("db");
        Assert.assertEquals(2, db.getValueForMetaPropertyToMany("schemas").size());

        int size = runtime.getModelRepository().serialize().length;

        for (int i = 0; i < 10; i++)
        {
            runtime.delete("sourceId.pure");
            try
            {
                runtime.compile();
                Assert.fail();
            }
            catch (Exception e)
            {
                Assert.assertEquals("Compilation error at (resource:userId.pure line:2 column:19), \"db has not been defined!\"", e.getMessage());
            }

            runtime.createInMemorySource("sourceId.pure", relationalDB);
            runtime.compile();
            Assert.assertEquals(size, repository.serialize().length);
            db = processorSupport.package_getByUserPath("db");
            Assert.assertEquals(2, db.getValueForMetaPropertyToMany("schemas").size());
        }
    }

    @Test
    public void testDatabaseIncludes() throws Exception
    {
        String content = "###Relational\n" +
                "Database db1\n" +
                "(\n" +
                "   Table myTable1\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   View myView1\n" +
                "   (\n" +
                "       myTableName : myTable1.name\n" +
                "   )\n" +
                ")\n" +
                "###Relational\n" +
                "Database db2\n" +
                "(\n" +
                "   Table myTable2\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   View myView2\n" +
                "   (\n" +
                "       myTableName : myTable2.name\n" +
                "   )\n" +
                ")\n";
        runtime.createInMemorySource("sourceId.pure", content);
        runtime.createInMemorySource("userId.pure", "###Relational\n" +
                "Database db\n" +
                "(" +
                "   include db1\n" +
                "   include db2\n" +
                "   Table myTable\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   Join myJoin(myTable1.name = myTable2.name)" +
                ")\n" +
                "###Pure\n" +
                "function test():Boolean[1]{assert(1 == db->meta::relational::metamodel::schema('default').tables->size(), |'');}");

        runtime.compile();
        int size = runtime.getModelRepository().serialize().length;

        for (int i = 0; i < 10; i++)
        {
            runtime.delete("sourceId.pure");
            try
            {
                runtime.compile();
                Assert.fail();
            }
            catch (Exception e)
            {
                Assert.assertEquals("Compilation error at (resource:userId.pure line:3 column:13), \"db1 has not been defined!\"", e.getMessage());
            }

            runtime.createInMemorySource("sourceId.pure", content);
            runtime.compile();
            Assert.assertEquals(size, runtime.getModelRepository().serialize().length);
        }
    }

    @Test
    public void testDatabaseIncludesWithExplicitDBReferenceInJoin() throws Exception
    {
        String content1 = "###Relational\n" +
                "Database test::db1\n" +
                "(\n" +
                "   Table myTable1\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   Table myTable2\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   View myView1\n" +
                "   (\n" +
                "       myTableName : myTable1.name\n" +
                "   )\n" +
                ")\n";
        String content2 = "###Relational\n" +
                "Database test::db1\n" +
                "(\n" +
                "   Table myTable1\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   Table myTable2\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   Table myTable3\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   View myView1\n" +
                "   (\n" +
                "       myTableName : myTable1.name\n" +
                "   )\n" +
                ")\n";
        runtime.createInMemorySource("sourceId.pure", content1);
        runtime.createInMemorySource("userId.pure", "###Relational\n" +
                "Database test::db\n" +
                "(" +
                "   include test::db1\n" +
                "   Table myTable\n" +
                "   (\n" +
                "       name VARCHAR(200) PRIMARY KEY\n" +
                "   )\n" +
                "   Join myJoin([test::db1]myTable1.name = myTable.name)" +
                ")\n" +
                "###Pure\n" +
                "import test::*;\n" +
                "function test():Boolean[1]\n" +
                "{\n" +
                "  assert(1 == db->meta::relational::metamodel::schema('default').tables->size(), |'');\n" +
                "}\n");

        runtime.compile();
        int size = runtime.getModelRepository().serialize().length;

        for (int i = 0; i < 10; i++)
        {
            runtime.modify("sourceId.pure", content2);
            runtime.compile();
            runtime.modify("sourceId.pure", content1);
            runtime.compile();
            Assert.assertEquals(size, runtime.getModelRepository().serialize().length);
        }
    }

    @Test
    public void testDatabaseWithIncludeAndSelfJoin()
    {
        String store1SourceId = "store1.pure";
        String store1Code = "###Relational\n" +
                "\n" +
                "Database test::TopDB\n" +
                "(\n" +
                ")";
        String store2SourceId = "store2.pure";
        String store2Code = "###Relational\n" +
                "\n" +
                "Database test::BottomDB\n" +
                "(\n" +
                "   include test::TopDB\n" +
                "   Table employee(id INT, name VARCHAR(200), manager INT)\n" +
                "   Join Managers(employee.manager = {target}.id)\n" +
                ")";
        compileTestSource(store1SourceId, store1Code);
        compileTestSource(store2SourceId, store2Code);
        int size = repository.serialize().length;

        for (int i = 0; i < 10; i++)
        {
            runtime.delete(store1SourceId);
            try
            {
                runtime.compile();
                Assert.fail("Expected compilation error");
            }
            catch (Exception e)
            {
                assertPureException(PureCompilationException.class, "test::TopDB has not been defined!", store2SourceId, 5, 18, e);
            }
            compileTestSource(store1SourceId, store1Code);
            Assert.assertEquals("Failed on iteration #" + i, size, repository.serialize().length);
        }
    }

    @Test
    public void testDatabaseWithIncludeSelfJoinAndSchema()
    {
        String store1SourceId = "store1.pure";
        String store1Code = "###Relational\n" +
                "\n" +
                "Database test::TopDB\n" +
                "(\n" +
                ")";
        String store2SourceId = "store2.pure";
        String store2Code = "###Relational\n" +
                "\n" +
                "Database test::BottomDB\n" +
                "(\n" +
                "   include test::TopDB\n" +
                "   Schema employees\n" +
                "   (\n" +
                "      Table employee(id INT, name VARCHAR(200), manager INT)\n" +
                "   )\n" +
                "   Join Managers(employees.employee.manager = {target}.id)\n" +
                ")";
        compileTestSource(store1SourceId, store1Code);
        compileTestSource(store2SourceId, store2Code);
        int size = repository.serialize().length;

        for (int i = 0; i < 10; i++)
        {
            runtime.delete(store1SourceId);
            try
            {
                runtime.compile();
                Assert.fail("Expected compilation error");
            }
            catch (Exception e)
            {
                assertPureException(PureCompilationException.class, "test::TopDB has not been defined!", store2SourceId, 5, 18, e);
            }
            compileTestSource(store1SourceId, store1Code);
            Assert.assertEquals("Failed on iteration #" + i, size, repository.serialize().length);
        }
    }

    @Test
    public void testDatabaseStereotype() throws Exception
    {
        String relationalDB = "###Pure\n" +
                "Profile meta::pure::profiles::storeType\n" +
                "{\n" +
                "    stereotypes: [type1, type2];\n" +
                "}\n" +
                "\n" +
                "Class <<meta::pure::profiles::storeType.type2>> test::Address\n" +
                "{\n" +
                "  country: String[1];\n" +
                "}\n" +
                "\n" +
                "###Relational\n" +
                "Database <<meta::pure::profiles::storeType.type2>> test::TestDB\n" +
                "(\n" +
                "  Table Product\n" +
                "  (\n" +
                "    ProductID VARCHAR(30) PRIMARY KEY\n" +
                "  )\n" +
                ")";
        runtime.createInMemorySource("sourceId.pure", relationalDB);
        runtime.compile();
        int size = runtime.getModelRepository().serialize().length;

        CoreInstance db = processorSupport.package_getByUserPath("test::TestDB");
        Assert.assertEquals(1, db.getValueForMetaPropertyToMany("schemas").size());
        Assert.assertEquals(size, runtime.getModelRepository().serialize().length);
    }
}
