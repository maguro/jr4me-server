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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import static org.reflections.util.ClasspathHelper.forPackage;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import static org.reflections.util.FilterBuilder.prefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolazydogs.jr4me.api.Param;
import com.toolazydogs.jr4me.server.jackson.BatchCallDeserializer;
import com.toolazydogs.jr4me.server.jackson.CamelCaseNamingStrategy;
import com.toolazydogs.jr4me.server.jackson.Deserializer;
import com.toolazydogs.jr4me.server.jackson.JacksonUtils;
import com.toolazydogs.jr4me.server.jackson.MethodParametersDeserializer;
import com.toolazydogs.jr4me.server.jackson.ParamDeserializer;


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

        List<MethodParametersDeserializer> deserializers = new ArrayList<MethodParametersDeserializer>();
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

            for (Method method : reflections.getMethodsAnnotatedWith(com.toolazydogs.jr4me.api.Method.class))
            {
                Class<?> declaringClass = method.getDeclaringClass();
                com.toolazydogs.jr4me.api.Method ann = method.getAnnotation(com.toolazydogs.jr4me.api.Method.class);
                ObjectMapper methodMapper = new ObjectMapper();
                methodMapper.setPropertyNamingStrategy(new CamelCaseNamingStrategy());

                List<ParamDeserializer> paramDeserializers = new ArrayList<ParamDeserializer>();
                for (int i = 0; i < method.getParameterTypes().length; i++)
                {
                    Class<?> parameterType = method.getParameterTypes()[i];
                    for (Annotation annotation : method.getParameterAnnotations()[i])
                    {
                        if (annotation instanceof com.toolazydogs.jr4me.api.Param)
                        {
                            com.toolazydogs.jr4me.api.Param param = (Param)annotation;
                            methodMapper.getDeserializationConfig().addMixInAnnotations(parameterType, declaringClass);
                            methodMapper.getSerializationConfig().addMixInAnnotations(parameterType, declaringClass);

                            paramDeserializers.add(JacksonUtils.createDeserializer(param.name(), parameterType, methodMapper));
                        }
                    }
                }
                deserializers.add(new MethodParametersDeserializer(ann.name(), paramDeserializers.toArray(new ParamDeserializer[paramDeserializers.size()])));
            }
            packages.add(pkg.trim());
        }

        mapper.registerModule(new SimpleModule("JsonRpcModule", new Version(1, 0, 0, null))
                                      .addDeserializer(Call.class, new Deserializer(deserializers.toArray(new MethodParametersDeserializer[deserializers.size()])))
                                      .addDeserializer(BatchCall.class, new BatchCallDeserializer()));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        BatchCall batchCall = mapper.readValue(req.getInputStream(), BatchCall.class);
        for (Call call : batchCall.getCalls())
        {

        }
    }
}
