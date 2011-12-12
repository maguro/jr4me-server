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

import java.lang.reflect.Method;

import com.google.common.base.Predicate;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import static org.reflections.util.ClasspathHelper.forPackage;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import static org.reflections.util.FilterBuilder.prefix;
import org.testng.annotations.Test;

import com.toolazydogs.jr4me.api.Param;


/**
 *
 */
public class AnnotationScanningTest
{
    @Test
    public void test() throws Exception
    {
        Predicate<String> filter = new FilterBuilder.Include(prefix("com.toolazydogs.jr4me.api"));
        String pkgs = "com.acme.service";
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
                Class<?> declaringClazz = method.getDeclaringClass();
                com.toolazydogs.jr4me.api.Method ann = method.getAnnotation(com.toolazydogs.jr4me.api.Method.class);
                System.err.println(method.getName() + "=" + ann.name());
                for (int i = 0; i < method.getParameterTypes().length; i++)
                {
                    com.toolazydogs.jr4me.api.Param param = (Param)method.getParameterAnnotations()[i][0];
                    Class<?> clazz = method.getParameterTypes()[i];
                    System.err.println(param.name() + "=" + clazz.toString());
                }
            }
        }
    }
}