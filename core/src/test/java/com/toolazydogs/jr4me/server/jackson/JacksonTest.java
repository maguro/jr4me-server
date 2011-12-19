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

import com.acme.pojo.Car;
import com.acme.pojo.Horse;
import com.acme.pojo.Vehicle;
import com.acme.svc.HorseDeserializer;
import com.acme.svc.HorseSerializer;
import com.acme.svc.Rpc;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.toolazydogs.jr4me.server.model.BatchCall;
import com.toolazydogs.jr4me.server.model.Call;
import com.toolazydogs.jr4me.server.model.CallParamArray;
import com.toolazydogs.jr4me.server.model.CallParamMap;


/**
 *
 */
public class JacksonTest
{
    ObjectMapper mapper;

    @Test
    public void testParameterArray() throws Exception
    {
        BatchCall calls = mapper.readValue("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [\"george\",  {\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\", \"engine\":{\"hp\":414}}], \"id\": 1}", BatchCall.class);
        assertNotNull(calls);
        assertEquals(calls.getCalls().length, 1);

        CallParamArray arrayCall = (CallParamArray)calls.getCalls()[0];
        assertNotNull(arrayCall);
        assertEquals(arrayCall.getJsonrpc(), "2.0");
        assertEquals(arrayCall.getMethod(), "subtract");
        assertEquals(arrayCall.getId(), Integer.valueOf(1));
        assertNotNull(arrayCall.getParams());
        assertEquals(arrayCall.getParams().length, 2);
        assertEquals(arrayCall.getParams()[0], "george");
        assertEquals(arrayCall.getParams()[1], new Car("speedy", "BMW", "M3", 414));

        System.out.println(mapper.writeValueAsString(calls));
    }

    @Test
    public void testParameterMap() throws Exception
    {
        BatchCall calls = mapper.readValue("{\"jsonrpc\": \"2.0\"," +
                                           " \"method\": \"subtract\", " +
                                           " \"params\": {\"name\":\"george\", \"vehicle\":{\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\", \"engine\":{\"hp\":414}}}, \"id\": 1}", BatchCall.class);
        assertNotNull(calls);
        assertEquals(calls.getCalls().length, 1);

        CallParamMap mapCall = (CallParamMap)calls.getCalls()[0];
        assertNotNull(mapCall);
        assertEquals(mapCall.getJsonrpc(), "2.0");
        assertEquals(mapCall.getMethod(), "subtract");
        assertEquals(mapCall.getId(), Integer.valueOf(1));
        assertNotNull(mapCall.getParams());
        assertEquals(mapCall.getParams().size(), 2);
        assertEquals(mapCall.getParams().get("name"), "george");
        assertEquals(mapCall.getParams().get("vehicle"), new Car("speedy", "BMW", "M3", 414));

        System.out.println(mapper.writeValueAsString(calls));
    }

    @Test
    public void testBatchCalls() throws Exception
    {
        BatchCall calls = mapper.readValue("[" +
                                           "{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [\"george\",  {\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\", \"engine\":{\"hp\":414}}], \"id\": 1}," +
                                           "{\"jsonrpc\": \"2.0\", \"method\": \"add\", \"params\": {\"name\":\"gracie\",  \"vehicle\":{\"type\":\"car\",\"name\":\"scoot\",\"make\":\"Mini\", \"model\":\"Cooper\", \"engine\":{\"hp\":121}}}, \"id\": 2}" +
                                           "]", BatchCall.class);
        assertNotNull(calls);
        assertEquals(calls.getCalls().length, 2);

        CallParamArray arrayCall = (CallParamArray)calls.getCalls()[0];
        assertNotNull(arrayCall);
        assertEquals(arrayCall.getJsonrpc(), "2.0");
        assertEquals(arrayCall.getMethod(), "subtract");
        assertEquals(arrayCall.getId(), Integer.valueOf(1));
        assertNotNull(arrayCall.getParams());
        assertEquals(arrayCall.getParams().length, 2);
        assertEquals(arrayCall.getParams()[0], "george");
        assertEquals(arrayCall.getParams()[1], new Car("speedy", "BMW", "M3", 414));

        CallParamMap mapCall = (CallParamMap)calls.getCalls()[1];
        assertNotNull(mapCall);
        assertEquals(mapCall.getJsonrpc(), "2.0");
        assertEquals(mapCall.getMethod(), "add");
        assertEquals(mapCall.getId(), Integer.valueOf(2));
        assertNotNull(mapCall.getParams());
        assertEquals(mapCall.getParams().size(), 2);
        assertEquals(mapCall.getParams().get("name"), "gracie");
        assertEquals(mapCall.getParams().get("vehicle"), new Car("scoot", "Mini", "Cooper", 121));

        System.out.println(mapper.writeValueAsString(calls));
    }

    @Test
    public void testHorseCodec() throws Exception
    {
        BatchCall calls = mapper.readValue("{\"jsonrpc\": \"2.0\", \"method\": \"create\", \"params\": [{\"name\":\"Flicka\"}], \"id\": 1}",
                                           BatchCall.class);
        assertNotNull(calls);
        assertEquals(calls.getCalls().length, 1);

        CallParamArray arrayCall = (CallParamArray)calls.getCalls()[0];
        assertNotNull(arrayCall);
        assertEquals(arrayCall.getJsonrpc(), "2.0");
        assertEquals(arrayCall.getMethod(), "create");
        assertEquals(arrayCall.getId(), Integer.valueOf(1));
        assertNotNull(arrayCall.getParams());
        assertEquals(arrayCall.getParams().length, 1);
        assertEquals(arrayCall.getParams()[0], Horse.fromString("Flicka"));

        System.out.println(mapper.writeValueAsString(calls));
    }

    @Test
    public void testBadCalls() throws Exception
    {
        try
        {
            mapper.readValue("ds", BatchCall.class);
            fail("Bad JSON object");
        }
        catch (JsonParseException ignored)
        {
            System.out.println(ignored);
        }

        try
        {
            mapper.readValue("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": {\"foo\" : \"bar\", \"name\":\"george\",  \"vehicle\":{\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\"}}, \"id\": 1}", BatchCall.class);
            fail("Bad JSON object w/ extraneous parameter foo");
        }
        catch (JsonMappingException ignored)
        {
            System.out.println(ignored);
        }

        try
        {
            mapper.readValue("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": {\"vehicle\":{\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\"}}, \"id\": 1}", BatchCall.class);
            fail("Bad JSON object w/ missing parameter name");
        }
        catch (JsonMappingException ignored)
        {
            System.out.println(ignored);
        }

        try
        {
            mapper.readValue("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [\"george\"], \"id\": 1}", BatchCall.class);
            fail("Bad JSON object w/ too little parameters");
        }
        catch (JsonMappingException ignored)
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
        methodMapper.registerModule(new SimpleModule("JsonRpcModule", new Version(1, 0, 0, null))
                                            .addDeserializer(Horse.class, new HorseDeserializer())
                                            .addSerializer(new HorseSerializer()));
        methodMapper.setPropertyNamingStrategy(new CamelCaseNamingStrategy());
        methodMapper.getDeserializationConfig().addMixInAnnotations(Vehicle.class, Rpc.class);
        methodMapper.getSerializationConfig().addMixInAnnotations(Vehicle.class, Rpc.class);

        MethodParametersDeserializer s = new MethodParametersDeserializer("subtract", new ParamDeserializer[]{new ParamDeserializerString("name", methodMapper),
                                                                                                              new ParamDeserializerObject("vehicle", Vehicle.class, methodMapper)});
        MethodParametersDeserializer a = new MethodParametersDeserializer("add", new ParamDeserializer[]{new ParamDeserializerString("name", methodMapper),
                                                                                                         new ParamDeserializerObject("vehicle", Vehicle.class, methodMapper)});
        MethodParametersDeserializer c = new MethodParametersDeserializer("create", new ParamDeserializer[]{new ParamDeserializerObject("parent", Horse.class, methodMapper)});

        mapper.registerModule(new SimpleModule("JsonRpcModule", new Version(1, 0, 0, null))
                                      .addDeserializer(Call.class, new Deserializer(new MethodParametersDeserializer[]{s, a, c}))
                                      .addDeserializer(BatchCall.class, new BatchCallDeserializer())
                                      .addDeserializer(Horse.class, new HorseDeserializer())
                                      .addSerializer(new HorseSerializer()));
    }

    @AfterTest
    public void afterTest()
    {
        System.out.flush();
    }
}
