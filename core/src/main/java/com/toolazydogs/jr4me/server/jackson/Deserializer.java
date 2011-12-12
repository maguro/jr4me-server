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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolazydogs.jr4me.server.model.Call;
import com.toolazydogs.jr4me.server.model.CallParam;
import com.toolazydogs.jr4me.server.model.CallParamArray;
import com.toolazydogs.jr4me.server.model.CallParamMap;


/**
 *
 */
public class Deserializer extends StdDeserializer<Call>
{
    static final Logger LOG = LoggerFactory.getLogger(Deserializer.class);
    private final Map<String, MethodParametersDeserializer> map = new HashMap<String, MethodParametersDeserializer>();

    public Deserializer(MethodParametersDeserializer[] deserializers)
    {
        super(CallParamArray.class);

        for (MethodParametersDeserializer deserializer : deserializers)
        {
            assert !map.containsKey(deserializer.getMethod());

            map.put(deserializer.getMethod(), deserializer);
        }

        LOG.trace("Configured with {} deserializers", deserializers.length);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Call deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException
    {
        CallParam call;
        String jsonrpc = null;
        String method = null;
        Object p = null;
        Integer id = null;

        JsonToken token = parser.getCurrentToken();
        if (token == JsonToken.START_OBJECT)
        {
            token = parser.nextToken();
            if (token != JsonToken.FIELD_NAME) throw context.wrongTokenException(parser, token, "Expected field name for JSON RPC");
        }
        else
        {
            throw context.wrongTokenException(parser, token, "Expected start of JSON object");
        }

        LOG.trace("Starting parse of JSON RPC object");

        while (token != JsonToken.END_OBJECT)
        {
            if (token == JsonToken.FIELD_NAME)
            {
                String name = parser.getText();

                if ("jsonrpc".equals(name))
                {
                    token = parser.nextToken();
                    if (token != JsonToken.VALUE_STRING) throw context.wrongTokenException(parser, token, "Expected string value for JSON RPC version");
                    jsonrpc = parser.getText();

                    LOG.trace("JSON RPC version {}", jsonrpc);
                }
                else if ("method".equals(name))
                {
                    token = parser.nextToken();
                    if (token != JsonToken.VALUE_STRING) throw context.wrongTokenException(parser, token, "Expected string value for JSON RPC method name");
                    method = parser.getText();

                    LOG.trace("JSON RPC method {}", method);
                }
                else if ("id".equals(name))
                {
                    token = parser.nextToken();
                    if (token != JsonToken.VALUE_NUMBER_INT) throw context.wrongTokenException(parser, token, "Expected int value for JSON RPC id");
                    id = parser.getIntValue();

                    LOG.trace("JSON RPC id {}", id);
                }
                else if ("params".equals(name))
                {
                    MethodParametersDeserializer deserializer = map.get(method);
                    if (deserializer == null) throw context.weirdStringException(CallParamMap.class, "Method " + method + " not a registered method");
                    p = deserializer.deserialize(parser, context);
                }
                else
                {
                    throw context.unknownFieldException(Call.class, name);
                }

                token = parser.nextToken();
            }
            else
            {
                throw context.wrongTokenException(parser, token, "Expected fiend name or end of JSON object");
            }
        }

        LOG.trace("Completed parse of JSON RPC object");

        if (p == null) throw context.instantiationException(Call.class, "Parameters were not set for JSON RPC");
        if (p instanceof List)
        {
            call = new CallParamArray();
            ((CallParamArray)call).setParams(((List<Object>)p).toArray());
        }
        else
        {
            call = new CallParamMap();
            ((CallParamMap)call).setParams((Map<String, Object>)p);
        }

        if (jsonrpc == null) throw context.instantiationException(Call.class, "Version was not set for JSON RPC");
        call.setJsonrpc(jsonrpc);

        if (method == null) throw context.instantiationException(Call.class, "Method was not set for JSON RPC");
        call.setMethod(method);

        call.setId(id);

        return call;
    }
}
