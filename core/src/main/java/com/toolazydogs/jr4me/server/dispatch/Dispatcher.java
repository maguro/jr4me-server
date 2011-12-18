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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolazydogs.jr4me.api.Jr4meException;
import com.toolazydogs.jr4me.server.ErrorCodes;
import com.toolazydogs.jr4me.server.model.Reply;
import com.toolazydogs.jr4me.server.model.ReplyError;
import com.toolazydogs.jr4me.server.model.ReplyResult;


/**
 *
 */
public class Dispatcher
{
    static final Logger LOG = LoggerFactory.getLogger(Dispatcher.class);
    private final Class<?> declaringClass;
    private final Method method;
    private final List<String> names;

    public Dispatcher(Class<?> declaringClass, Method method, List<String> names)
    {
        assert declaringClass != null;
        assert method != null;
        assert names != null;

        this.declaringClass = declaringClass;
        this.method = method;
        this.names = names;
    }

    public Reply call(Object[] params, int id, BeanManager beanManager)
    {
        LOG.trace("Calling {} for id {}", method.toString(), id);

        try
        {
            Object object = getContextualInstance(beanManager, declaringClass);
            Object response = method.invoke(object, params);
            return new ReplyResult(response, id);
        }
        catch (IllegalAccessException e)
        {
            return new ReplyError(ErrorCodes.METHOD_INSTANTIATION_ERROR, id);
        }
        catch (InstantiationException e)
        {
            return new ReplyError(ErrorCodes.METHOD_INSTANTIATION_ERROR, id);
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof Jr4meException)
            {
                Jr4meException je = (Jr4meException)e.getCause();
                return new ReplyError(je.getCode(), je.getMessage(), id);
            }
            else
            {
                return new ReplyError(ErrorCodes.METHOD_INVOCATION_ERROR, id);
            }
        }
    }

    public Reply call(Map<String, Object> params, int id, BeanManager beanManager)
    {
        LOG.trace("Calling {} for id {}", method.toString(), id);

        List<Object> list = new ArrayList<Object>(names.size());
        for (String name : names)
        {
            list.add(params.get(name));
        }

        try
        {
            Object object = getContextualInstance(beanManager, declaringClass);
            Object response = method.invoke(object, list.toArray());
            return new ReplyResult(response, id);
        }
        catch (IllegalAccessException e)
        {
            return new ReplyError(ErrorCodes.METHOD_INSTANTIATION_ERROR, id);
        }
        catch (InstantiationException e)
        {
            return new ReplyError(ErrorCodes.METHOD_INSTANTIATION_ERROR, id);
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof Jr4meException)
            {
                Jr4meException je = (Jr4meException)e.getCause();
                return new ReplyError(je.getCode(), je.getMessage(), id);
            }
            else
            {
                return new ReplyError(ErrorCodes.METHOD_INVOCATION_ERROR, id);
            }
        }
    }

    public static Object getContextualInstance(final BeanManager manager, final Class type) throws IllegalAccessException, InstantiationException
    {
        Object result = null;
        if (manager != null)
        {
            Bean bean = manager.resolve(manager.getBeans(type));
            if (bean != null)
            {
                CreationalContext context = manager.createCreationalContext(bean);
                if (context != null)
                {
                    result = manager.getReference(bean, type, context);
                }
            }
        }
        else
        {
            result = type.newInstance();
        }
        return result;
    }
}
