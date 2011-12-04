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
import java.util.List;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class JsonRpcBatchCallDeserializer extends StdDeserializer<JsonRpcBatchCall>
{
    static final Logger LOG = LoggerFactory.getLogger(JsonRpcBatchCall.class);

    public JsonRpcBatchCallDeserializer()
    {
        super(JsonRpcCallParamArray.class);
    }

    @Override
    public JsonRpcBatchCall deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException
    {
        ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        JsonRpcBatchCall batch = new JsonRpcBatchCall();
        List<JsonRpcCall> calls = new ArrayList<JsonRpcCall>();

        JsonToken token = parser.getCurrentToken();
        if (token == JsonToken.START_OBJECT)
        {
            calls.add(mapper.readValue(parser, JsonRpcCall.class));
        }
        else if (token == JsonToken.START_ARRAY)
        {
            token = parser.nextToken();
            while (token != JsonToken.END_ARRAY)
            {
                calls.add(mapper.readValue(parser, JsonRpcCall.class));
                token = parser.nextToken();
            }

        }

        batch.setCalls(calls.toArray(new JsonRpcCall[calls.size()]));

        return batch;
    }
}
