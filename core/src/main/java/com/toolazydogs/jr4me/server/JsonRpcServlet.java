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

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
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
import java.util.WeakHashMap;

import com.google.common.base.Predicate;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.module.SimpleModule;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import static org.reflections.util.ClasspathHelper.forPackage;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import static org.reflections.util.FilterBuilder.prefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolazydogs.jr4me.api.Codecs;
import com.toolazydogs.jr4me.api.MapException;
import com.toolazydogs.jr4me.api.Param;
import com.toolazydogs.jr4me.server.dispatch.Dispatcher;
import com.toolazydogs.jr4me.server.jackson.BatchCallDeserializer;
import com.toolazydogs.jr4me.server.jackson.CamelCaseNamingStrategy;
import com.toolazydogs.jr4me.server.jackson.Deserializer;
import com.toolazydogs.jr4me.server.jackson.JacksonUtils;
import com.toolazydogs.jr4me.server.jackson.MethodParametersDeserializer;
import com.toolazydogs.jr4me.server.jackson.ParamDeserializer;
import com.toolazydogs.jr4me.server.model.BatchCall;
import com.toolazydogs.jr4me.server.model.Call;
import com.toolazydogs.jr4me.server.model.CallError;
import com.toolazydogs.jr4me.server.model.CallParamArray;
import com.toolazydogs.jr4me.server.model.CallParamMap;
import com.toolazydogs.jr4me.server.model.Reply;
import com.toolazydogs.jr4me.server.model.ReplyError;


/**
 *
 */
public class JsonRpcServlet extends HttpServlet
{
    private final static Logger LOGGER = LoggerFactory.getLogger(JsonRpcServlet.class);
    public static String PACKAGES = "com.toolazydogs.jr4me.packages";
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Class, com.toolazydogs.jr4me.server.model.Error> errors = new WeakHashMap<Class, com.toolazydogs.jr4me.server.model.Error>();
    private final Map<String, Dispatcher> methods = new HashMap<String, Dispatcher>();
    @Inject private BeanManager beanManager;

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
                            .setUrls(forPackage(pkg))
                            .setScanners(new MethodAnnotationsScanner().filterResultsBy(filter))
            );

            /**
             * Scrape classes for annotations that declare mappings of exceptions to JSON-RPC errors
             */
            for (Class clazz : reflections.getTypesAnnotatedWith(com.toolazydogs.jr4me.api.MapException.class))
            {
                MapException mapException = (MapException)clazz.getAnnotation(MapException.class);
                for (MapException.Map map : mapException.value())
                {
                    errors.put(map.exception(), new com.toolazydogs.jr4me.server.model.Error(map.code(), map.message()));
                }
            }

            /**
             * Scrape classes for annotations that declare codecs for JSON-RPC de/serialization.
             *
             * This module will be passed on, shared, to all the individual method object mappers.
             */
            Set<Class> codecSpecified = new HashSet<Class>();
            SimpleModule methodModule = new SimpleModule("JsonRpcModule", new Version(1, 0, 0, null));
            for (Class clazz : reflections.getTypesAnnotatedWith(com.toolazydogs.jr4me.api.Codecs.class))
            {
                Codecs codecs = (Codecs)clazz.getAnnotation(Codecs.class);
                for (Codecs.Codec codec : codecs.value())
                {
                    codecSpecified.add(codec.clazz());

                    if (codec.serializer() != null)
                    {
                        try
                        {
                            methodModule.addSerializer((JsonSerializer)codec.serializer().newInstance());
                        }
                        catch (InstantiationException e)
                        {
                            LOGGER.warn("Unable to instantiate {}", codec.serializer().getName());
                        }
                        catch (IllegalAccessException e)
                        {
                            LOGGER.warn("Unable to access {}", codec.serializer().getName());
                        }
                    }
                    if (codec.deserializer() != null)
                    {
                        try
                        {
                            StdDeserializer deserializer = (StdDeserializer)codec.deserializer().newInstance();
                            methodModule.addDeserializer(deserializer.getValueClass(), deserializer);
                        }
                        catch (InstantiationException e)
                        {
                            LOGGER.warn("Unable to instantiate {}", codec.deserializer().getName());
                        }
                        catch (IllegalAccessException e)
                        {
                            LOGGER.warn("Unable to access {}", codec.deserializer().getName());
                        }
                    }
                }
            }

            for (Method method : reflections.getMethodsAnnotatedWith(com.toolazydogs.jr4me.api.Method.class))
            {
                Class<?> declaringClass = method.getDeclaringClass();
                com.toolazydogs.jr4me.api.Method ann = method.getAnnotation(com.toolazydogs.jr4me.api.Method.class);
                ObjectMapper methodMapper = new ObjectMapper();
                methodMapper.setPropertyNamingStrategy(new CamelCaseNamingStrategy());
                methodMapper.registerModule(methodModule);

                LOGGER.trace("Found annotated method {}", method.toString());

                List<ParamDeserializer> paramDeserializers = new ArrayList<ParamDeserializer>();
                List<String> names = new ArrayList<String>();
                for (int i = 0; i < method.getParameterTypes().length; i++)
                {
                    Class<?> parameterType = method.getParameterTypes()[i];
                    String name = null;
                    for (Annotation annotation : method.getParameterAnnotations()[i])
                    {
                        if (annotation instanceof com.toolazydogs.jr4me.api.Param)
                        {
                            com.toolazydogs.jr4me.api.Param param = (Param)annotation;

                            /**
                             * Types with explicitly declared codecs should not participate in the mixins
                             */
                            if (!codecSpecified.contains(parameterType))
                            {
                                methodMapper.getDeserializationConfig().addMixInAnnotations(parameterType, declaringClass);
                                methodMapper.getSerializationConfig().addMixInAnnotations(parameterType, declaringClass);
                            }

                            paramDeserializers.add(JacksonUtils.createDeserializer(param.name(), parameterType, methodMapper));
                            name = param.name();
                        }
                    }
                    if (name != null)
                    {
                        names.add(name);
                    }
                    else
                    {
                        throw new ServletException("Missing Param annotation");
                    }
                }

                String name = (ann.name().equals(com.toolazydogs.jr4me.api.Method.USE_METHOD_NAME) ? method.getName() : ann.name());
                deserializers.add(new MethodParametersDeserializer(name, paramDeserializers.toArray(new ParamDeserializer[paramDeserializers.size()])));
                mapper.getSerializationConfig().addMixInAnnotations(method.getReturnType(), declaringClass);

                methods.put(name, new Dispatcher(declaringClass, method, names));
            }
        }

        mapper.registerModule(new SimpleModule("JsonRpcModule", new Version(1, 0, 0, null))
                                      .addDeserializer(Call.class, new Deserializer(deserializers.toArray(new MethodParametersDeserializer[deserializers.size()])))
                                      .addDeserializer(BatchCall.class, new BatchCallDeserializer()));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("application/json");

        List<Reply> responses = new ArrayList<Reply>();
        try
        {
            BatchCall batchCall = mapper.readValue(request.getInputStream(), BatchCall.class);

            LOGGER.trace("Received {} calls", batchCall.getCalls().length);

            for (Call c : batchCall.getCalls())
            {
                try
                {
                    if (c instanceof CallParamArray)
                    {
                        CallParamArray call = (CallParamArray)c;
                        Dispatcher dispatcher = methods.get(call.getMethod());
                        responses.add(dispatcher.call(call.getParams(), call.getId(), beanManager));
                    }
                    else if (c instanceof CallParamMap)
                    {
                        CallParamMap call = (CallParamMap)c;
                        Dispatcher dispatcher = methods.get(call.getMethod());
                        responses.add(dispatcher.call(call.getParams(), call.getId(), beanManager));
                    }
                    else
                    {
                        CallError error = (CallError)c;
                        responses.add(new ReplyError(error.getError(), error.getId()));
                    }
                }
                catch (Throwable t)
                {
                    boolean found = false;
                    for (Class clazz : errors.keySet())
                    {
                        if (t.getClass() == clazz)
                        {
                            responses.add(new ReplyError(errors.get(clazz), c.getId()));
                            found = true;
                            break;
                        }
                    }
                    if (!found) responses.add(new ReplyError(ErrorCodes.METHOD_ERROR, c.getId()));
                }
            }
        }
        catch (JsonProcessingException jpe)
        {
            responses.add(new ReplyError(ErrorCodes.INVALID_REQUEST, null));
        }
        catch (Throwable t)
        {
            responses.add(new ReplyError(ErrorCodes.INTERNAL_ERROR, null));
        }

        if (responses.size() == 1)
        {
            mapper.writeValue(response.getOutputStream(), responses.get(0));
        }
        else
        {
            mapper.writeValue(response.getOutputStream(), responses.toArray(new Reply[responses.size()]));
        }
    }
}
