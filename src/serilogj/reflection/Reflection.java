package serilogj.reflection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.lang.reflect.*;
import serilogj.reflection.Property;
import serilogj.reflection.FieldProperty;
import serilogj.reflection.MethodProperty;

// Copyright 2013-2015 Serilog Contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Values in Serilog are simplified down into a lowest-common-denominator internal
// type system so that there is a better chance of code written with one sink in
// mind working correctly with any other. This technique also makes the programmer
// writing a log event (roughly) in control of the cost of recording that event.
@SuppressWarnings("unchecked")
public class Reflection {
    /**
     * This reflection class was based on https://github.com/boonproject/boon/
     */

    private static Map<Class<?>, Map<String, Property>> _properties = new ConcurrentHashMap<>( 200 );

    /**
     * This flattens a list.
     * @param o object that might be a list
     * @param list list to add o to or all of o's items to.
     * @return an object or a list
     */
    public static Object unifyListOrArray(Object o, List list) {
        if (o==null) {
            return null;
        }

        boolean isArray = o.getClass().isArray();
        if (list == null && !isArray && !(o instanceof Iterable)) {
            return o;
        }

        if (list == null) {
            list = new LinkedList();
        }

        if (isArray) {
            int length = Array.getLength( o );
            for (int index = 0; index < length; index++) {
                Object o1 = Array.get(o, index);
                if (o1 instanceof Iterable || o.getClass().isArray()) {
                    unifyListOrArray(o1, list);
                } else {
                    list.add(o1);
                }
            }
        } else if (o instanceof Collection) {
            Collection i = ((Collection) o);
            for (Object item : i) {

                if (item instanceof Iterable || o.getClass().isArray()) {
                    unifyListOrArray(item, list);
                } else {
                    list.add(item);
                }
            }
        } else {
            list.add(o);
        }

        return list;
    }

    public static Map<String, Property> getProperties(Class<?> type) {
        Map<String, Property> properties = _properties.get(type);
        if (properties != null) {
            return properties;
        }

        properties = getAllFieldProperties(type);
        for (Map.Entry<String, Property> entry : getAllMethodProperties(type).entrySet())
        {
            if (properties.get(entry.getKey()) != null) {
                continue;
            }

            properties.put(entry.getKey(), entry.getValue());
        }

        _properties.put(type, properties);
        return properties;
    }

    private static Map<String, Property> getAllFieldProperties(Class<?> type) {
        Map<String, Property> fields = new HashMap<>();
        for (Property entry : getFieldProperties(type)) {
            fields.put(entry.getAlias(), entry);
        }

        while (type != Object.class) {
            type = type.getSuperclass();
            for (Property entry : getFieldProperties(type)) {
                if (fields.get(entry.getAlias()) != null) {
                    continue;
                }
                fields.put(entry.getAlias(), entry);
            }
        }

        return fields;
    }

    private static List<Property> getFieldProperties(Class<?> type) {
        List<Property> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers)) {
                continue;
            }

            String name = field.getName();
            if (name.indexOf("$") != -1) {
                continue;
            }

            field.setAccessible(true);
            fields.add(new FieldProperty(field, name));
        }

        return fields;
    }

    private static Map<String, Property> getAllMethodProperties(Class<?> type) {
        Map<String, Property> methods = new HashMap<>();

        for (Method method : type.getMethods()) {
            if (method.getParameterTypes().length > 0) {
                continue;
            }

            if (method.getReturnType() == Void.class) {
                continue;
            }

            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers)) {
                continue;
            }
            
            String alias = null;
            String name = method.getName();
            if (name.equals("getClass") || name.equals("is") || name.equals("get")) {
                continue;
            }

            if (name.startsWith("is")) {
                alias = name.substring(2);
            } else if (name.startsWith("get")) {
                alias = name.substring(3);
            }
            
            if (alias == null) {
                continue;
            }

            alias = alias.substring(0, 1).toLowerCase() + (alias.length() > 1 ? alias.substring(1) : "");
            
            method.setAccessible(true);
            methods.put(alias, new MethodProperty(method, alias));
        }

        return methods;
    }
}