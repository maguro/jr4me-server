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
import java.lang.reflect.Type;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

import com.toolazydogs.jr4me.server.Call;


/**
 *
 */
public class CallSerializer extends SerializerBase<Call>
{
    public CallSerializer()
    {
        super(Call.class);
    }

    @Override
    public void serialize(Call value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException
    {
        jgen.writeStartObject();

        jgen.writeFieldName("jsonrpc");
        jgen.writeString(value.getJsonrpc());
        jgen.writeFieldName("method");
        jgen.writeString(value.getMethod());

        jgen.writeFieldName("params");

        jgen.writeObject(value.getParams());

        jgen.writeNumberField("id", value.getId());

        jgen.writeEndObject();
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint)
    {
        return createSchemaNode("object", false);
    }
}
