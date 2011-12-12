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

import com.toolazydogs.jr4me.server.model.Call;
import com.toolazydogs.jr4me.server.model.CallParamMap;


/**
 *
 */
public class MethodParametersDeserializer extends StdDeserializer<Object>
{
    static final Logger LOG = LoggerFactory.getLogger(MethodParametersDeserializer.class);
    private final String method;
    private final ParamDeserializer[] array;
    private final Map<String, ParamDeserializer> map = new HashMap<String, ParamDeserializer>();

    public MethodParametersDeserializer(String method, ParamDeserializer[] deserializers)
    {
        super(Object.class);

        assert method != null;

        this.method = method;
        this.array = new ParamDeserializer[deserializers.length];
        System.arraycopy(deserializers, 0, this.array, 0, deserializers.length);

        for (ParamDeserializer deserializer : deserializers)
        {
            assert !map.containsKey(deserializer.getKey());

            map.put(deserializer.getKey(), deserializer);
        }

        LOG.trace("Configured with {} deserializers", deserializers.length);
    }

    public String getMethod()
    {
        return method;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException
    {
        Call call;

        JsonToken token = parser.getCurrentToken();

        token = parser.nextToken();
        if (token == JsonToken.START_ARRAY)
        {
            LOG.trace("JSON RPC object has array of parameters");

            List<Object> params = new ArrayList<Object>(array.length);

            for (ParamDeserializer deserializer : array)
            {
                Object parameter = deserializer.deserialize(parser, context);
                params.add(parameter);
                LOG.trace("  parameter {}", parameter);
            }

            token = parser.nextToken();
            if (token != JsonToken.END_ARRAY) throw context.wrongTokenException(parser, token, "Expected end of param array for JSON RPC");

            return params;
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
                    String k = parser.getText();
                    ParamDeserializer deserializer = map.get(k);

                    if (deserializer == null) throw context.weirdStringException(CallParamMap.class, "Key " + k + " not used in method " + method);

                    Object v = deserializer.deserialize(parser, context);
                    params.put(k, v);
                    LOG.trace("  parameter [{},{}]", k, v);
                }
                else
                {
                    throw context.wrongTokenException(parser, token, "Expected fiend name or end of JSON object");
                }
                token = parser.nextToken();
            }
            if (token != JsonToken.END_OBJECT) throw context.wrongTokenException(parser, token, "Expected end of param map for JSON RPC");

            for (String key : this.map.keySet())
            {
                if (!params.containsKey(key)) throw context.instantiationException(Call.class, "Parameter " + key + " was not set for JSON RPC");
            }
            return params;
        }
        else
        {
            throw context.wrongTokenException(parser, token, "Expected start of param array or map for JSON RPC");
        }
    }
}
