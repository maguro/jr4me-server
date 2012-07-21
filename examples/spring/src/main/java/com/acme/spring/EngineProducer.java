/**
 *
 * Copyright 2012 (C) The original author or authors
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
package com.acme.spring;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.acme.model.Engine;
import org.jboss.seam.spring.context.Configuration;
import org.jboss.seam.spring.context.SpringContext;
import org.jboss.seam.spring.inject.SpringBean;
import org.springframework.context.ApplicationContext;


/**
 *
 */
public class EngineProducer
{
    @Produces
    @SpringContext
    @Configuration(locations = "classpath*:applicationContext.xml")
    @ApplicationScoped
    ApplicationContext context;

    @Produces
    @SpringBean
    @ApplicationScoped
    Engine engine;
}
