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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getInputStream()).thenReturn(new ServletInputStream()
        {
            InputStream in = new ByteArrayInputStream("{\"jsonrpc\": \"2.0\", \"method\": \"foo\", \"params\": [\"george\",  {\"type\":\"car\",\"name\":\"speedy\",\"make\":\"BMW\", \"model\":\"M3\"}], \"id\": 1}".getBytes());

            @Override
            public int read() throws IOException
            {
                return in.read();
            }
        });
        HttpServletResponse resp = mock(HttpServletResponse.class);

        servlet.init(config);
        servlet.doPost(req, resp);
    }

    public static void main(String... args) throws Exception
    {
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder servletHolder = new ServletHolder(new JsonRpcServlet());
        servletHolder.setInitParameter(JsonRpcServlet.PACKAGES, "com.acme.service");
        servletHolder.getRegistration().setLoadOnStartup(1);

        Enumeration<URL> enumeration = JsonRpcServletTest.class.getClassLoader().getResources("org/livetribe/jmx/rest/web");
        while (enumeration.hasMoreElements())
        {
            URL url = enumeration.nextElement();
            System.err.println(url);
        }

        context.addServlet(servletHolder, "/ws/*");

        server.start();
        server.join();
    }
}
