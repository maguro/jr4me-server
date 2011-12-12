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
package com.toolazydogs.jr4me.server.jackson;

import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;


/**
 * Converts standard CamelCase field and method names to
 * typical JSON field names having all lower case characters
 * with an underscore separating different words.  For
 * example, all of the following are converted to JSON field
 * name "some_name":
 * <p/>
 * Java field name "someName"
 * Java method name "getSomeName"
 * Java method name "setSomeName"
 * <p/>
 * Typical Use:
 * <p/>
 * String jsonString = "{\"foo_name\":\"fubar\"}";
 * ObjectMapper mapper = new ObjectMapper();
 * mapper.setPropertyNamingStrategy(
 * new CamelCaseNamingStrategy());
 * Foo foo = mapper.readValue(jsonString, Foo.class);
 * System.out.println(mapper.writeValueAsString(foo));
 * // prints {"foo_name":"fubar"}
 * <p/>
 * class Foo
 * {
 * private String fooName;
 * public String getFooName() {return fooName;}
 * public void setFooName(String fooName)
 * {this.fooName = fooName;}
 * }
 */
public class CamelCaseNamingStrategy extends PropertyNamingStrategy
{
    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName)
    {
        return translate(defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName)
    {
        return translate(defaultName);
    }

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName)
    {
        return translate(defaultName);
    }

    private String translate(String defaultName)
    {
        char[] nameChars = defaultName.toCharArray();
        StringBuilder nameTranslated =
                new StringBuilder(nameChars.length * 2);
        for (char c : nameChars)
        {
            if (Character.isUpperCase(c))
            {
                nameTranslated.append("_");
                c = Character.toLowerCase(c);
            }
            nameTranslated.append(c);
        }
        return nameTranslated.toString();
    }
}