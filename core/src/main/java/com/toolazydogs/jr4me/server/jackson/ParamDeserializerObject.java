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

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class ParamDeserializerObject extends ParamDeserializer
{
    static final Logger LOG = LoggerFactory.getLogger(ParamDeserializerObject.class);
    private final Class<?> clazz;

    public ParamDeserializerObject(String key, Class<?> clazz, ObjectMapper mapper)
    {
        super(key, mapper);
        assert clazz != null;
        this.clazz = clazz;
    }

    public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException
    {
        JsonToken token = parser.nextToken();
        if (token == JsonToken.VALUE_NULL) return null;
        if (token != JsonToken.START_OBJECT) throw context.wrongTokenException(parser, token, "Expected object for JSON RPC parameter " + getKey());

        Object value = getMapper().readValue(parser, clazz);

        LOG.trace("Adding {} of class {} to parameters", value, clazz);

        return value;
    }
}
