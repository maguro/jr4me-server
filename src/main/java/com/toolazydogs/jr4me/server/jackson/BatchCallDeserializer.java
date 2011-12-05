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

import com.toolazydogs.jr4me.server.BatchCall;
import com.toolazydogs.jr4me.server.Call;
import com.toolazydogs.jr4me.server.CallParamArray;


/**
 *
 */
public class BatchCallDeserializer extends StdDeserializer<BatchCall>
{
    static final Logger LOG = LoggerFactory.getLogger(BatchCall.class);

    public BatchCallDeserializer()
    {
        super(CallParamArray.class);
    }

    @Override
    public BatchCall deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException
    {
        ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        BatchCall batch = new BatchCall();
        List<Call> calls = new ArrayList<Call>();

        JsonToken token = parser.getCurrentToken();
        if (token == JsonToken.START_OBJECT)
        {
            LOG.trace("Found single call");
            calls.add(mapper.readValue(parser, Call.class));
        }
        else if (token == JsonToken.START_ARRAY)
        {
            LOG.trace("Found batch of calls");
            token = parser.nextToken();
            while (token != JsonToken.END_ARRAY)
            {
                calls.add(mapper.readValue(parser, Call.class));
                token = parser.nextToken();
            }
        }

        batch.setCalls(calls.toArray(new Call[calls.size()]));

        return batch;
    }
}
