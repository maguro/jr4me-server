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
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;


/**
 *
 */
public class JsonRpcServletTest
{
    @Test
    public void testParamsArray() throws Exception
    {
        JsonRpcServlet servlet = new JsonRpcServlet();
        ServletConfig config = mock(ServletConfig.class);

        when(config.getInitParameter(JsonRpcServlet.PACKAGES)).thenReturn("com.acme.svc");
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

        assertEquals(out.toString(), "{\"jsonrpc\":\"2.0\",\"result\":\"george:speedy\",\"id\":1}");
    }

    @Test
    public void testParamsMap() throws Exception
    {
        JsonRpcServlet servlet = new JsonRpcServlet();
        ServletConfig config = mock(ServletConfig.class);

        when(config.getInitParameter(JsonRpcServlet.PACKAGES)).thenReturn("com.acme.svc");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(new ServletInputStream()
        {
            InputStream in = new ByteArrayInputStream("{\"jsonrpc\": \"2.0\", \"method\": \"register\", \"params\": {\"name\": \"george\",  \"vehicle\": {\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\"}}, \"id\": 1}".getBytes());

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

        assertEquals(out.toString(), "{\"jsonrpc\":\"2.0\",\"result\":\"george:speedy\",\"id\":1}");
    }

    @Test
    public void testCodec() throws Exception
    {
        JsonRpcServlet servlet = new JsonRpcServlet();
        ServletConfig config = mock(ServletConfig.class);

        when(config.getInitParameter(JsonRpcServlet.PACKAGES)).thenReturn("com.acme.svc");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(new ServletInputStream()
        {
            InputStream in = new ByteArrayInputStream("{\"jsonrpc\": \"2.0\", \"method\": \"create\", \"params\": [{\"name\":\"Flicka\"}], \"id\": 1}".getBytes());

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

        assertEquals(out.toString(), "{\"jsonrpc\":\"2.0\",\"result\":{\"name\":\"child of Flicka\"},\"id\":1}");
    }

    @Test
    public void testAttribute() throws Exception
    {
        JsonRpcServlet servlet = new JsonRpcServlet();
        ServletConfig config = mock(ServletConfig.class);

        when(config.getInitParameter(JsonRpcServlet.PACKAGES)).thenReturn("com.acme.svc");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(new ServletInputStream()
        {
            InputStream in = new ByteArrayInputStream("{\"jsonrpc\": \"2.0\", \"method\": \"set\", \"params\": [{\"name\":\"Flicka\", \"value\":[\"Foo\", {\"car\":\"cdr\"}]}], \"id\": 1}".getBytes());

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

        assertEquals(out.toString(), "{\"jsonrpc\":\"2.0\",\"result\":{\"name\":\"Flicka\",\"value\":[\"Foo\",{\"car\":\"cdr\"}]},\"id\":1}");
    }

    @Test
    public void testError() throws Exception
    {
        JsonRpcServlet servlet = new JsonRpcServlet();
        ServletConfig config = mock(ServletConfig.class);

        when(config.getInitParameter(JsonRpcServlet.PACKAGES)).thenReturn("com.acme.svc");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(new ServletInputStream()
        {
            InputStream in = new ByteArrayInputStream("{\"jsonrpc\": \"2.0\", \"method\": \"bad\", \"params\": [], \"id\": 1}".getBytes());

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

        assertEquals(out.toString(), "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-1,\"message\":\"Error\"},\"id\":1}");
    }

    @Test
    public void testNpe() throws Exception
    {
        JsonRpcServlet servlet = new JsonRpcServlet();
        ServletConfig config = mock(ServletConfig.class);

        when(config.getInitParameter(JsonRpcServlet.PACKAGES)).thenReturn("com.acme.svc");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(new ServletInputStream()
        {
            InputStream in = new ByteArrayInputStream("{\"jsonrpc\": \"2.0\", \"method\": \"npe\", \"params\": [], \"id\": 1}".getBytes());

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

        assertEquals(out.toString(), "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-2,\"message\":\"NPE\"},\"id\":1}");
    }
}
