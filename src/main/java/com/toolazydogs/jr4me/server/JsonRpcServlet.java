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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import org.codehaus.jackson.map.ObjectMapper;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import static org.reflections.util.ClasspathHelper.forPackage;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import static org.reflections.util.FilterBuilder.prefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolazydogs.jr4me.api.Param;
import com.toolazydogs.jr4me.server.jackson.CamelCaseNamingStrategy;


/**
 *
 */
public class JsonRpcServlet extends HttpServlet
{
    private final static Logger LOGGER = LoggerFactory.getLogger(JsonRpcServlet.class);
    public static String PACKAGES = "com.toolazydogs.jr4me.packages";
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> packages = new HashSet<String>();
    private final Map<String, Object> methods = new HashMap<String, Object>();

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        mapper.setPropertyNamingStrategy(new CamelCaseNamingStrategy());

        Predicate<String> filter = new FilterBuilder.Include(prefix("com.toolazydogs.jr4me.api"));
        String pkgs = config.getInitParameter(PACKAGES);
        if (pkgs == null) throw new ServletException(PACKAGES + " not set");
        for (String pkg : pkgs.split(","))
        {
            pkg = pkg.trim();
            Reflections reflections = new Reflections(
                    new ConfigurationBuilder()
                            .setUrls(forPackage(pkgs))
                            .setScanners(new MethodAnnotationsScanner().filterResultsBy(filter))
            );

            for (Method rpc : reflections.getMethodsAnnotatedWith(com.toolazydogs.jr4me.api.Method.class))
            {
                com.toolazydogs.jr4me.api.Method ann = rpc.getAnnotation(com.toolazydogs.jr4me.api.Method.class);
                System.err.println("rpc:" + rpc.getName() + ":" + ann.name());
                for (int i = 0; i < rpc.getParameterTypes().length; i++)
                {
                    com.toolazydogs.jr4me.api.Param param = (Param)rpc.getParameterAnnotations()[i][0];
                    System.err.println(param.name());
                }
            }
            packages.add(pkg.trim());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        BatchCall call = mapper.readValue(req.getInputStream(), BatchCall.class);
    }
}
