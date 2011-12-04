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


/**
 *
 */
public abstract class JsonRpcCall
{
    public static final int NOT_SET = -1;
    private String jsonrpc;
    private String method;
    private int id = NOT_SET;

    public abstract Object getParams();

    public String getJsonrpc()
    {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc)
    {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
}
