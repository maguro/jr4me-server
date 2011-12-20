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

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolazydogs.jr4me.server.jackson.Deserializer;
import com.toolazydogs.jr4me.server.jackson.Serializer;


/**
 *
 */
public class AttributeSerializer extends SerializerBase<Attribute>
{
    static final Logger LOG = LoggerFactory.getLogger(AttributeSerializer.class);

    public AttributeSerializer()
    {
        super(Attribute.class);
    }

    @Override
    public void serialize(Attribute attribute, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException
    {
        LOG.trace("Serializing attribute {}", attribute);

        generator.writeStartObject();
        generator.writeStringField("name", attribute.getName());
        generator.writeFieldName("value");

        ObjectMapper mapper = Serializer.getMapper();
        if (mapper != null)
        {
            mapper.writeValue(generator, attribute.getValue());
        }
        else
        {
            LOG.warn("Mapper not set by Deserializer");
        }

        generator.writeEndObject();
    }
}
