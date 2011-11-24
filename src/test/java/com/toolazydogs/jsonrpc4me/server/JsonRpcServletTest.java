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
package com.toolazydogs.jsonrpc4me.server;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.testng.annotations.Test;


/**
 * @author Alan D. Cabrera
 */
public class JsonRpcServletTest
{
    @Test
    public void test() throws Exception
    {
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder servletHolder = new ServletHolder(new JsonRpcServlet());
        servletHolder.setInitParameter(JsonRpcServlet.PACKAGES, "com.acme.service");
        servletHolder.getRegistration().setLoadOnStartup(1);

        try
        {
            Enumeration<URL> enumeration =  this.getClass().getClassLoader().getResources("org/livetribe/jmx/rest/web");
            while (enumeration.hasMoreElements())
            {
                URL url = enumeration.nextElement();
                System.err.println(url);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();  //Todo change body of catch statement use File | Settings | File Templates.
        }

        context.addServlet(servletHolder, "/ws/*");

        try
        {
            server.start();
            server.join();
        }
        catch (Exception e)
        {
            throw new IOException("Unable to start Jetty server", e);
        }
    }

}
