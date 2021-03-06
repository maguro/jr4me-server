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

import javax.management.Attribute;
import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolazydogs.jr4me.server.jackson.Deserializer;
import com.toolazydogs.jr4me.server.model.Call;


/**
 *
 */
public class AttributeDeserializer extends StdDeserializer<Attribute>
{
    static final Logger LOG = LoggerFactory.getLogger(AttributeDeserializer.class);

    public AttributeDeserializer()
    {
        super(Attribute.class);
    }

    @Override
    public Attribute deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException
    {
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

        LOG.trace("Starting parse of ObjectInstance");

        String name = null;
        Object value = null;
        while (token != JsonToken.END_OBJECT)
        {
            if (token == JsonToken.FIELD_NAME)
            {
                String fieldName = parser.getText();

                if ("name".equals(fieldName))
                {
                    token = parser.nextToken();
                    if (token != JsonToken.VALUE_STRING) throw context.wrongTokenException(parser, token, "Expected string value for name");
                    name = parser.getText();

                    LOG.trace("ObjectName name {}", name);
                }
                else if ("value".equals(fieldName))
                {
                    parser.nextToken();

                    ObjectMapper mapper = Deserializer.getMapper();
                    if (mapper != null)
                    {
                        value = mapper.readValue(parser, Object.class);
                    }
                    else
                    {
                        LOG.warn("Mapper not set by Deserializer");
                        value = null;
                    }

                    LOG.trace("Class name {}", value);
                }
                else
                {
                    throw context.unknownFieldException(Call.class, fieldName);
                }

                token = parser.nextToken();
            }
            else
            {
                throw context.wrongTokenException(parser, token, "Expected field name or end of JSON object");
            }
        }

        LOG.trace("Completed parse of ObjectInstance");

        if (name == null) throw context.instantiationException(Call.class, "Attribute name was not set");
        if (value == null) throw context.instantiationException(Call.class, "Attribute value was not set");

        return new Attribute(name, value);
    }
}
