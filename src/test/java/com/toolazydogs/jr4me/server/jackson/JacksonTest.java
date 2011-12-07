/**
 * Copyright 2011 (C) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.toolazydogs.jr4me.server.jackson;

import java.io.IOException;

import com.acme.model.Car;
import com.acme.model.Vehicle;
import com.acme.service.Rpc;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.toolazydogs.jr4me.server.BatchCall;
import com.toolazydogs.jr4me.server.Call;
import com.toolazydogs.jr4me.server.CallParamArray;
import com.toolazydogs.jr4me.server.CallParamMap;


/**
 *
 */
public class JacksonTest
{
    ObjectMapper mapper;

    @Test
    public void testParameterArray() throws Exception
    {
        BatchCall calls = mapper.readValue("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [\"george\",  {\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\"}], \"id\": 1}", BatchCall.class);
        assertNotNull(calls);
        assertEquals(calls.getCalls().length, 1);

        CallParamArray arrayCall = (CallParamArray)calls.getCalls()[0];
        assertNotNull(arrayCall);
        assertEquals(arrayCall.getJsonrpc(), "2.0");
        assertEquals(arrayCall.getMethod(), "subtract");
        assertEquals(arrayCall.getId(), 1);
        assertNotNull(arrayCall.getParams());
        assertEquals(arrayCall.getParams().length, 2);
        assertEquals(arrayCall.getParams()[0], "george");
        assertEquals(arrayCall.getParams()[1], new Car("speedy", "BMW", "M3"));

        System.out.println(mapper.writeValueAsString(calls));
    }

    @Test
    public void testParameterMap() throws Exception
    {
        BatchCall calls = mapper.readValue("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": {\"name\":\"george\",  \"vehicle\":{\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\"}}, \"id\": 1}", BatchCall.class);
        assertNotNull(calls);
        assertEquals(calls.getCalls().length, 1);

        CallParamMap mapCall = (CallParamMap)calls.getCalls()[0];
        assertNotNull(mapCall);
        assertEquals(mapCall.getJsonrpc(), "2.0");
        assertEquals(mapCall.getMethod(), "subtract");
        assertEquals(mapCall.getId(), 1);
        assertNotNull(mapCall.getParams());
        assertEquals(mapCall.getParams().size(), 2);
        assertEquals(mapCall.getParams().get("name"), "george");
        assertEquals(mapCall.getParams().get("vehicle"), new Car("speedy", "BMW", "M3"));

        System.out.println(mapper.writeValueAsString(calls));
    }

    @Test
    public void testBatchCalls() throws Exception
    {
        BatchCall calls = mapper.readValue("[" +
                                           "{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [\"george\",  {\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\"}], \"id\": 1}," +
                                           "{\"jsonrpc\": \"2.0\", \"method\": \"add\", \"params\": {\"name\":\"gracie\",  \"vehicle\":{\"type\":\"car\",\"name\":\"scoot\",\"make\":\"Mini\", \"model\":\"Cooper\"}}, \"id\": 2}" +
                                           "]", BatchCall.class);
        assertNotNull(calls);
        assertEquals(calls.getCalls().length, 2);

        CallParamArray arrayCall = (CallParamArray)calls.getCalls()[0];
        assertNotNull(arrayCall);
        assertEquals(arrayCall.getJsonrpc(), "2.0");
        assertEquals(arrayCall.getMethod(), "subtract");
        assertEquals(arrayCall.getId(), 1);
        assertNotNull(arrayCall.getParams());
        assertEquals(arrayCall.getParams().length, 2);
        assertEquals(arrayCall.getParams()[0], "george");
        assertEquals(arrayCall.getParams()[1], new Car("speedy", "BMW", "M3"));

        CallParamMap mapCall = (CallParamMap)calls.getCalls()[1];
        assertNotNull(mapCall);
        assertEquals(mapCall.getJsonrpc(), "2.0");
        assertEquals(mapCall.getMethod(), "add");
        assertEquals(mapCall.getId(), 2);
        assertNotNull(mapCall.getParams());
        assertEquals(mapCall.getParams().size(), 2);
        assertEquals(mapCall.getParams().get("name"), "gracie");
        assertEquals(mapCall.getParams().get("vehicle"), new Car("scoot", "Mini", "Cooper"));

        System.out.println(mapper.writeValueAsString(calls));
    }

    @Test
    public void testBadCalls() throws Exception
    {
        try
        {
            mapper.readValue("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": {\"foo\" : \"bar\", \"name\":\"george\",  \"vehicle\":{\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\"}}, \"id\": 1}", BatchCall.class);
            fail("Bad JSON object w/ extraneous parameter foo");
        }
        catch (IOException ignored)
        {
            System.out.println(ignored);

        }

        try
        {
            mapper.readValue("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": {\"vehicle\":{\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\"}}, \"id\": 1}", BatchCall.class);
            fail("Bad JSON object w/ missing parameter name");
        }
        catch (IOException ignored)
        {
            System.out.println(ignored);

        }

        try
        {
            mapper.readValue("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [\"george\"], \"id\": 1}", BatchCall.class);
            fail("Bad JSON object w/ too little parameters");
        }
        catch (IOException ignored)
        {
            System.out.println(ignored);
        }
    }

    @BeforeClass
    public void beforeClass() throws Exception
    {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new CamelCaseNamingStrategy());

        ObjectMapper methodMapper = new ObjectMapper();
        methodMapper.setPropertyNamingStrategy(new CamelCaseNamingStrategy());
        methodMapper.getDeserializationConfig().addMixInAnnotations(Vehicle.class, Rpc.class);
        methodMapper.getSerializationConfig().addMixInAnnotations(Vehicle.class, Rpc.class);
        MethodParametersDeserializer s = new MethodParametersDeserializer("subtract", new ParamDeserializer[]{new ParamDeserializerString("name", methodMapper),
                                                                                                              new ParamDeserializerObject("vehicle", Vehicle.class, methodMapper)});
        MethodParametersDeserializer a = new MethodParametersDeserializer("add", new ParamDeserializer[]{new ParamDeserializerString("name", methodMapper),
                                                                                                         new ParamDeserializerObject("vehicle", Vehicle.class, methodMapper)});

        mapper.registerModule(new SimpleModule("JsonRpcModule", new Version(1, 0, 0, null))
                                      .addDeserializer(Call.class, new Deserializer(new MethodParametersDeserializer[]{s, a}))
                                      .addDeserializer(BatchCall.class, new BatchCallDeserializer()));
    }

    @AfterTest
    public void afterTest()
    {
        System.out.flush();
    }
}
