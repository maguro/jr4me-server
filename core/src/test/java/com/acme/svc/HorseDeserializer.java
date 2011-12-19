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
package com.acme.svc;

import java.io.IOException;

import com.acme.pojo.Horse;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolazydogs.jr4me.server.model.Call;


/**
 *
 */
public class HorseDeserializer extends StdDeserializer<Horse>
{
    static final Logger LOG = LoggerFactory.getLogger(HorseDeserializer.class);

    public HorseDeserializer()
    {
        super(Horse.class);
    }

    @Override
    public Horse deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException
    {
        ObjectMapper mapper = (ObjectMapper)parser.getCodec();

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

        LOG.trace("Starting parse of horse");

        String horse = null;
        while (token != JsonToken.END_OBJECT)
        {
            if (token == JsonToken.FIELD_NAME)
            {
                String name = parser.getText();

                if ("name".equals(name))
                {
                    token = parser.nextToken();
                    if (token != JsonToken.VALUE_STRING) throw context.wrongTokenException(parser, token, "Expected string value for horse name");
                    horse = parser.getText();

                    LOG.trace("Horse name {}", horse);
                }
                else
                {
                    throw context.unknownFieldException(Call.class, name);
                }

                token = parser.nextToken();
            }
            else
            {
                throw context.wrongTokenException(parser, token, "Expected field name or end of JSON object");
            }
        }

        LOG.trace("Completed parse of horse");


        if (horse == null) throw context.instantiationException(Call.class, "Horse name was not set");

        return Horse.fromString(horse);
    }
}
