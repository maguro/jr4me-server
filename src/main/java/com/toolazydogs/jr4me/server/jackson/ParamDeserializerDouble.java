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
public class ParamDeserializerDouble extends ParamDeserializer
{
    static final Logger LOG = LoggerFactory.getLogger(ParamDeserializerDouble.class);

    public ParamDeserializerDouble(String key, ObjectMapper mapper)
    {
        super(key, mapper);
    }

    public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException
    {
        JsonToken token = parser.nextToken();
        if (token != JsonToken.VALUE_NUMBER_FLOAT) throw context.wrongTokenException(parser, token, "Expected double value for JSON RPC parameter " + getKey());

        double value = parser.getDoubleValue();

        LOG.trace("Adding {} to parameters", value);

        return value;
    }
}
