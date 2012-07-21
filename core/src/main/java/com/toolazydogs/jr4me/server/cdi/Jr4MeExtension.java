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
package com.toolazydogs.jr4me.server.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.ProcessProducerField;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import static org.reflections.util.ClasspathHelper.forPackage;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import static org.reflections.util.FilterBuilder.prefix;


/**
 *
 */
public class Jr4MeExtension implements Extension
{
    public void ProcessProducerField(@Observes ProcessProducerField ppf, BeanManager bm)
    {
        int i = 0;
    }

    public void ProcessProducer(@Observes ProcessProducer pp, BeanManager bm)
    {
        int i = 0;
    }

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager bm)
    {
        for (final Class<?> clazz : getBeanClasses())
        {
            AnnotatedType<?> at = bm.createAnnotatedType(clazz);
            bbd.addAnnotatedType(at);
        }
    }

    protected List<Class> getBeanClasses()
    {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(forPackage(""))
                        .setScanners(new MethodAnnotationsScanner().filterResultsBy(new FilterBuilder.Include(prefix("com.toolazydogs.jr4me.api"))),
                                     new FieldAnnotationsScanner().filterResultsBy(new FilterBuilder.Include(prefix("javax.enterprise.inject"))))
        );

        Set<Class<?>> classes = new HashSet<Class<?>>();
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        for (Method method : reflections.getMethodsAnnotatedWith(com.toolazydogs.jr4me.api.Method.class))
        {
            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass.isInterface())
            {
                interfaces.add(declaringClass);
            }
            else
            {
                classes.add(declaringClass);
            }
        }

        for (Class<?> interfaze : interfaces)
        {
            for (Class<?> subType : reflections.getSubTypesOf(interfaze))
            {
                classes.add(subType);
            }
        }

        for (Field field : reflections.getFieldsAnnotatedWith(Produces.class))
        {
            classes.add(field.getDeclaringClass());
        }

        for (Method method : reflections.getMethodsAnnotatedWith(Produces.class))
        {
            classes.add(method.getDeclaringClass());
        }

        return Arrays.asList(classes.toArray(new Class[classes.size()]));
    }
}
