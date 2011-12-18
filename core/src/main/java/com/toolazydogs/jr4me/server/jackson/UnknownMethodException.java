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

import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class UnknownMethodException extends JsonMappingException
{
    public UnknownMethodException(String msg)
    {
        super(msg);
    }

    public UnknownMethodException(String msg, Throwable rootCause)
    {
        super(msg, rootCause);
    }

    public UnknownMethodException(String msg, JsonLocation loc)
    {
        super(msg, loc);
    }

    public UnknownMethodException(String msg, JsonLocation loc, Throwable rootCause)
    {
        super(msg, loc, rootCause);
    }
}
