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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public Dispatcher(Class<?> declaringClass, Method method)
    {
        this.declaringClass = declaringClass;
        this.method = method;
    }

    public Reply call(Object[] params, int id, BeanManager beanManager)
    {
        try
        {
//            AnnotatedType at = beanManager.createAnnotatedType(declaringClass);
//            //use this to create the class and inject dependencies
//            final InjectionTarget it = beanManager.createInjectionTarget(at);
//            DispatcherBean dispatcherBean = new DispatcherBean(declaringClass, it);
//            CreationalContext context = beanManager.createCreationalContext(dispatcherBean);
//            Object object = null;
//            if (context != null)
//            {
//                object = beanManager.getReference(dispatcherBean, declaringClass, context);
//            }

            Object object = getContextualInstance(beanManager, declaringClass);
            Object o2 = getContextualInstance(beanManager, declaringClass);
            Object response = method.invoke(object, params);
            return new ReplyResult(response, id);
        }
        catch (IllegalAccessException e)
        {
            return new ReplyError(ErrorCodes.METHOD_INSTANTIATION_ERROR, id);
        }
        catch (InvocationTargetException e)
        {
            return new ReplyError(ErrorCodes.METHOD_INVOCATION_ERROR, id);
        }
    }

    public ReplyResult call(Map<String, Object> params, int id, BeanManager beanManager)
    {
        return new ReplyResult("HELLO MAP", id);  //Todo change body of created methods use File | Settings | File Templates.
    }

    public static <T> T getContextualInstance(final BeanManager manager, final Class<T> type)
    {
        T result = null;
        Bean<T> bean = (Bean<T>)manager.resolve(manager.getBeans(type));
        if (bean != null)
        {
            CreationalContext<T> context = manager.createCreationalContext(bean);
            if (context != null)
            {
                result = (T)manager.getReference(bean, type, context);
            }
        }
        return result;
    }
}
