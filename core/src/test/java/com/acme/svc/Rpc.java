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
package com.acme.svc;

import javax.inject.Inject;
import javax.management.Attribute;

import com.acme.pojo.Car;
import com.acme.pojo.Carriage;
import com.acme.pojo.Engine;
import com.acme.pojo.Horse;
import com.acme.pojo.Vehicle;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.toolazydogs.jr4me.api.Codecs;
import com.toolazydogs.jr4me.api.MapException;
import com.toolazydogs.jr4me.api.Method;
import com.toolazydogs.jr4me.api.Param;


/**
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.PROPERTY,
              property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = Car.class, name = "car"),
               @JsonSubTypes.Type(value = Carriage.class, name = "carriage")})
@Codecs({@Codecs.Codec(clazz = Horse.class, serializer = HorseSerializer.class, deserializer = HorseDeserializer.class),
         @Codecs.Codec(clazz = Attribute.class, serializer = AttributeSerializer.class, deserializer = AttributeDeserializer.class)})
@MapException(@MapException.Map(exception = NullPointerException.class, code = -2, message = "NPE"))
public class Rpc
{
    @Inject Engine engine;

    @Method(name = "register")
    public String fooMethod(@Param(name = "name") String param, @Param(name = "vehicle") Vehicle vehicle)
    {
        return param + ":" + vehicle.getName() + (engine == null ? "" : ":" + engine.getHp());
    }

    @Method(name = "bad")
    public String badMethod() throws RpcException
    {
        throw new RpcException();
    }

    @Method(name = "npe")
    public String npeMethod()
    {
        throw new NullPointerException();
    }

    @Method
    public Horse create(@Param(name = "parent") Horse parent)
    {
        return Horse.fromString("child of " + parent.getName());
    }

    @Method
    public Attribute set(@Param(name = "attribute") Attribute attribute)
    {
        return attribute;
    }
}
