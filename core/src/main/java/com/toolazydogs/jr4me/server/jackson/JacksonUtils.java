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

import org.codehaus.jackson.map.ObjectMapper;


/**
 *
 */
public class JacksonUtils
{
    private JacksonUtils() { }

    public static ParamDeserializer createDeserializer(String name, Class<?> parameterType, ObjectMapper mapper)
    {
        if (Double.class == parameterType) return new ParamDeserializerDouble(name, mapper);
        if (Double.TYPE == parameterType) return new ParamDeserializerDouble(name, mapper);
        if (Float.class == parameterType) return new ParamDeserializerFloat(name, mapper);
        if (Float.TYPE == parameterType) return new ParamDeserializerFloat(name, mapper);
        if (Integer.class == parameterType) return new ParamDeserializerInteger(name, mapper);
        if (Integer.TYPE == parameterType) return new ParamDeserializerInteger(name, mapper);
        if (Long.class == parameterType) return new ParamDeserializerLong(name, mapper);
        if (Long.TYPE == parameterType) return new ParamDeserializerLong(name, mapper);
        if (String.class == parameterType) return new ParamDeserializerString(name, mapper);
        return new ParamDeserializerObject(name, parameterType, mapper);
    }
}
