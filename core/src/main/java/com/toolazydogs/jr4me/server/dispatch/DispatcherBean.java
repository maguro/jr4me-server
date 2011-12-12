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
package com.toolazydogs.jr4me.server.dispatch;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class DispatcherBean implements Bean
{
    static final Logger LOG = LoggerFactory.getLogger(DispatcherBean.class);

    private final Class clazz;
    private final InjectionTarget it;

    public DispatcherBean(Class clazz, InjectionTarget it)
    {
        this.clazz = clazz;
        this.it = it;
    }

    @Override
    public Class<?> getBeanClass()
    {
        return clazz;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        return it.getInjectionPoints();
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public Set<Annotation> getQualifiers()
    {
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        qualifiers.add(new AnnotationLiteral<Default>()
        {
        });
        qualifiers.add(new AnnotationLiteral<Any>()
        {
        });
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return Dependent.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        return Collections.emptySet();
    }

    @Override
    public Set<Type> getTypes()
    {
        Set<Type> types = new HashSet<Type>();
        types.add(clazz);
        types.add(Object.class);
        return types;
    }

    @Override
    public boolean isAlternative()
    {
        return false;
    }

    @Override
    public boolean isNullable()
    {
        return false;
    }

    @Override
    public Object create(CreationalContext ctx)
    {
        Object instance = it.produce(ctx);
        it.inject(instance, ctx);
        it.postConstruct(instance);
        return instance;
    }

    @Override
    public void destroy(Object instance, CreationalContext ctx)
    {
        it.preDestroy(instance);
        it.dispose(instance);
        ctx.release();
    }
}
