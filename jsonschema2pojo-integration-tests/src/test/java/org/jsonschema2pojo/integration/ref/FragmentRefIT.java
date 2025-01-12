/**
 * Copyright © 2010-2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.integration.ref;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class FragmentRefIT {

    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static Class<?> fragmentRefsClass;

    @BeforeClass
    public static void generateAndCompileEnum() throws ClassNotFoundException {

        ClassLoader fragmentRefsClassLoader = classSchemaRule.generateAndCompile("/schema/ref/fragmentRefs.json", "com.example");

        fragmentRefsClass = fragmentRefsClassLoader.loadClass("com.example.FragmentRefs");

    }

    @Test
    public void refToFragmentOfSelfIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = fragmentRefsClass.getMethod("getFragmentOfSelf").getReturnType();

        assertThat(aClass.getName(), is("com.example.A"));
        assertThat(aClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getPropertyOfA"))));
    }

    @Test
    public void refToFragmentOfAnotherSchemaIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = fragmentRefsClass.getMethod("getFragmentOfA").getReturnType();

        assertThat(aClass.getName(), is("com.example.AdditionalPropertyValue"));

    }

    @Test
    public void refToFragmentOfAnotherSchemaThatAlsoHasARefIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = fragmentRefsClass.getMethod("getFragmentWithAnotherRef").getReturnType();

        assertThat(aClass.getName(), is("java.lang.String"));

    }

    @Test
    public void selfRefWithoutParentFile() throws IOException {
        JCodeModel codeModel = new JCodeModel();
        JsonNode schema = new ObjectMapper().readTree("{\"type\":\"object\", \"properties\":{\"a\":{\"$ref\":\"#/b\"}}, \"b\":\"string\"}");

        JPackage p = codeModel._package("com.example");
        new RuleFactory().getSchemaRule().apply("Example", schema, null, p, new Schema(null, schema, null));
    }

    @Test
    public void refToInnerFragmentThatHasRefToOuterFragmentWithoutParentFile() throws IOException {
        JCodeModel codeModel = new JCodeModel();
        JsonNode schema = new ObjectMapper().readTree("{\n" +
                "    \"type\": \"object\",\n" +
                "    \"definitions\": {\n" +
                "        \"location\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"properties\": {\n" +
                "                \"cat\": {\n" +
                "                    \"$ref\": \"#/definitions/cat\"\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"cat\": {\n" +
                "            \"type\": \"number\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"properties\": {\n" +
                "        \"location\": {\n" +
                "            \"$ref\": \"#/definitions/location\"\n" +
                "        }\n" +
                "    }\n" +
                "}");

        JPackage p = codeModel._package("com.example");
        new RuleFactory().getSchemaRule().apply("Example", schema, null, p, new Schema(null, schema, null));
    }

}
