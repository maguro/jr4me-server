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
import java.util.ArrayList;
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


/**
 *
 */
public class JsonRpcDeserializer extends StdDeserializer<JsonRpcCall>
{
    static final Logger LOG = LoggerFactory.getLogger(JsonRpcDeserializer.class);
    private final JsonRpcParamDeserializer[] array;
    private final Map<String, JsonRpcParamDeserializer> map = new HashMap<String, JsonRpcParamDeserializer>();

    public JsonRpcDeserializer(JsonRpcParamDeserializer[] deserializers)
    {
        super(JsonRpcCallParamArray.class);

        this.array = new JsonRpcParamDeserializer[deserializers.length];
        System.arraycopy(deserializers, 0, this.array, 0, deserializers.length);

        for (JsonRpcParamDeserializer deserializer : deserializers)
        {
            assert !map.containsKey(deserializer.getKey());

            map.put(deserializer.getKey(), deserializer);
        }

        LOG.trace("Configured with {} deserializers", deserializers.length);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonRpcCall deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException
    {
        JsonRpcCall jsonRpcCall;
        String jsonrpc = null;
        String method = null;
        Object p = null;
        int id = JsonRpcCall.NOT_SET;

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
                token = parser.nextToken();

                if ("jsonrpc".equals(name))
                {
                    if (token != JsonToken.VALUE_STRING) throw context.wrongTokenException(parser, token, "Expected string value for JSON RPC version");
                    jsonrpc = parser.getText();

                    LOG.trace("JSON RPC version {}", jsonrpc);
                }
                else if ("method".equals(name))
                {
                    if (token != JsonToken.VALUE_STRING) throw context.wrongTokenException(parser, token, "Expected string value for JSON RPC method name");
                    method = parser.getText();

                    LOG.trace("JSON RPC method {}", method);
                }
                else if ("id".equals(name))
                {
                    if (token != JsonToken.VALUE_NUMBER_INT) throw context.wrongTokenException(parser, token, "Expected int value for JSON RPC id");
                    id = parser.getIntValue();

                    LOG.trace("JSON RPC id {}", id);
                }
                else if ("params".equals(name))
                {
                    if (token == JsonToken.START_ARRAY)
                    {
                        LOG.trace("JSON RPC object has array of parameters");

                        List<Object> params = new ArrayList<Object>(2);

                        for (JsonRpcParamDeserializer deserializer : array)
                        {
                            Object parameter = deserializer.deserialize(parser, context);
                            params.add(parameter);
                            LOG.trace("  parameter {}", parameter);
                        }

                        token = parser.nextToken();
                        if (token != JsonToken.END_ARRAY) throw context.wrongTokenException(parser, token, "Expected end of param array for JSON RPC");

                        p = params;
                    }
                    else if (token == JsonToken.START_OBJECT)
                    {
                        LOG.trace("JSON RPC object has map of parameters");

                        Map<String, Object> params = new HashMap<String, Object>();

                        token = parser.nextToken();
                        while (token != JsonToken.END_OBJECT)
                        {
                            if (token == JsonToken.FIELD_NAME)
                            {
                                String key = parser.getText();
                                JsonRpcParamDeserializer deserializer = map.get(key);

                                if (deserializer == null) throw context.weirdStringException(JsonRpcCallParamMap.class, "Key " + key + " not used in method");

                                Object value = deserializer.deserialize(parser, context);
                                params.put(key, value);
                                LOG.trace("  parameter [{},{}]", key, value);
                            }
                            else
                            {
                                throw context.wrongTokenException(parser, token, "Expected fiend name or end of JSON object");
                            }
                            token = parser.nextToken();
                        }
                        if (token != JsonToken.END_OBJECT) throw context.wrongTokenException(parser, token, "Expected end of param map for JSON RPC");
                        p = params;
                    }
                    else
                    {
                        throw context.wrongTokenException(parser, token, "Expected start of param array or map for JSON RPC");
                    }

                }
                else
                {
                    throw context.unknownFieldException(JsonRpcCall.class, name);
                }

                token = parser.nextToken();
            }
            else
            {
                throw context.wrongTokenException(parser, token, "Expected fiend name or end of JSON object");
            }
        }

        LOG.trace("Completed parse of JSON RPC object");

        if (p == null) throw context.instantiationException(JsonRpcCall.class, "Parameters were not set for JSON RPC");
        if (p instanceof List)
        {
            jsonRpcCall = new JsonRpcCallParamArray();

            ((JsonRpcCallParamArray)jsonRpcCall).setParams(((List<Object>)p).toArray());
        }
        else
        {
            for (String key : this.map.keySet())
            {
                if (!((Map<String, Object>)p).containsKey(key)) throw context.instantiationException(JsonRpcCall.class, "Parameter " + key + " was not set for JSON RPC");
            }
            jsonRpcCall = new JsonRpcCallParamMap();
            ((JsonRpcCallParamMap)jsonRpcCall).setParams((Map<String, Object>)p);
        }

        if (jsonrpc == null) throw context.instantiationException(JsonRpcCall.class, "Version was not set for JSON RPC");
        jsonRpcCall.setJsonrpc(jsonrpc);

        if (method == null) throw context.instantiationException(JsonRpcCall.class, "Method was not set for JSON RPC");
        jsonRpcCall.setMethod(method);

        jsonRpcCall.setId(id);

        return jsonRpcCall;
    }
}
