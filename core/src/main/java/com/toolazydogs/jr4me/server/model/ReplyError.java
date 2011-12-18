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
package com.toolazydogs.jr4me.server.model;


import com.toolazydogs.jr4me.server.ErrorCodes;


/**
 *
 */
public class ReplyError extends Reply
{
    private Error error;
    public Integer id;

    public ReplyError()
    {
    }

    public ReplyError(ErrorCodes code, Integer id)
    {
        this(code.getCode(), code.getMessage(), id);
    }

    public ReplyError(int code, String message, Integer id)
    {
        this.error = new Error(code, message);
        this.id = id;
    }

    public ReplyError(Error error, Integer id)
    {
        this.error = error;
        this.id = id;
    }

    public Error getError()
    {
        return error;
    }

    public void setError(Error error)
    {
        this.error = error;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }
}
