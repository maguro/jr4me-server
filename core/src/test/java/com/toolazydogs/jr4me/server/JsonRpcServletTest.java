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
package com.toolazydogs.jr4me.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testng.annotations.Test;


/**
 *
 */
public class JsonRpcServletTest
{
    @Test
    public void test() throws Exception
    {
        JsonRpcServlet servlet = new JsonRpcServlet();
        ServletConfig config = mock(ServletConfig.class);

        when(config.getInitParameter(JsonRpcServlet.PACKAGES)).thenReturn("com.acme.service");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(new ServletInputStream()
        {
            InputStream in = new ByteArrayInputStream("{\"jsonrpc\": \"2.0\", \"method\": \"register\", \"params\": [\"george\",  {\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\"}], \"id\": 1}".getBytes());

            @Override
            public int read() throws IOException
            {
                return in.read();
            }
        });
        HttpServletResponse response = mock(HttpServletResponse.class);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new ServletOutputStream()
        {
            @Override
            public void write(int b) throws IOException
            {
                out.write(b);
            }
        });

        servlet.init(config);
        servlet.doPost(request, response);
        System.out.println(out.toString());
    }
}
