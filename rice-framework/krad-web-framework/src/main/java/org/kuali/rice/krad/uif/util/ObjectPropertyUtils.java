/**
 * Copyright 2005-2014 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.uif.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.kuali.rice.krad.uif.util.ObjectPathExpressionParser.PathEntry;

/**
 * Utility methods to get/set property values and working with objects.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public final class ObjectPropertyUtils {
    private static final Logger LOG = Logger.getLogger(ObjectPropertyUtils.class);

    /**
     * Internal metadata cache.
     * 
     * <p>
     * NOTE: WeakHashMap is used as the internal cache representation. Since class objects are used
     * as the keys, this allows property descriptors to stay in cache until the class loader is
     * unloaded, but will not prevent the class loader itself from unloading. PropertyDescriptor
     * instances do not hold hard references back to the classes they refer to, so weak value
     * maintenance is not necessary.
     * </p>
     */
    private static final Map<Class<?>, ObjectPropertyMetadata> METADATA_CACHE = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, ObjectPropertyMetadata>(2048));

    /**
     * Get a mapping of property descriptors by property name for a bean class.
     * 
     * @param beanClass The bean class.
     * @return A mapping of all property descriptors for the bean class, by property name.
     */
    public static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> beanClass) {
        return getMetadata(beanClass).propertyDescriptors;
    }

    /**
     * Get a property descriptor from a class by property name.
     * 
     * @param beanClass The bean class.
     * @param propertyName The bean property name.
     * @return The property descriptor named on the bean class.
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> beanClass, String propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException("Null property name");
        }

        PropertyDescriptor propertyDescriptor = getPropertyDescriptors(beanClass).get(propertyName);
        if (propertyDescriptor != null) {
            return propertyDescriptor;
        } else {
            throw new IllegalArgumentException("Property " + propertyName
                    + " not found for bean " + beanClass);
        }
    }

    /**
     * Gets the names of all readable properties for the bean class.
     * 
     * @param beanClass The bean class.
     * @return set of property names
     */
    public static Set<String> getReadablePropertyNames(Class<?> beanClass) {
        return getMetadata(beanClass).readMethods.keySet();
    }

    /**
     * Get the read method for a specific property on a bean class.
     * 
     * @param beanClass The bean class.
     * @param propertyName The property name.
     * @return The read method for the property.
     */
    public static Method getReadMethod(Class<?> beanClass, String propertyName) {
        return getMetadata(beanClass).readMethods.get(propertyName);
    }

    /**
     * Get the read method for a specific property on a bean class.
     * 
     * @param beanClass The bean class.
     * @param propertyName The property name.
     * @return The read method for the property.
     */
    public static Method getWriteMethod(Class<?> beanClass, String propertyName) {
        return getMetadata(beanClass).writeMethods.get(propertyName);
    }

    /**
     * Copy properties from a string map to an object.
     * 
     * @param properties The string map. The keys of this map must be valid property path
     *        expressions in the context of the target object. The values are the string
     *        representations of the target bean properties.
     * @param object The target object, to copy the property values to.
     * @see ObjectPathExpressionParser
     */
    public static void copyPropertiesToObject(Map<String, String> properties, Object object) {
        for (Map.Entry<String, String> property : properties.entrySet()) {
            setPropertyValue(object, property.getKey(), property.getValue());
        }
    }

    /**
     * Get the type of a bean property.
     * 
     * <p>
     * Note that this method does not instantiate the bean class before performing introspection, so
     * will not dynamic initialization behavior into account. When dynamic initialization is needed
     * to accurate inspect the inferred property type, use {@link #getPropertyType(Object, String)}
     * instead of this method. This method is, however, intended for use on the implementation
     * class; to avoid instantiation simply to infer the property type, consider overriding the
     * return type on the property read method.
     * </p>
     * 
     * @param beanClass The bean class.
     * @param propertyPath A valid property path expression in the context of the bean class.
     * @return The property type referred to by the provided bean class and property path.
     * @see ObjectPathExpressionParser
     */
    public static Class<?> getPropertyType(Class<?> beanClass, String propertyPath) {
        try {
            ObjectPropertyReference.setWarning(true);
            return ObjectPropertyReference.resolvePath(null, beanClass, propertyPath, false).getPropertyType();
        } finally {
            ObjectPropertyReference.setWarning(false);
        }
    }

    /**
     * Get the type of a bean property.
     * 
     * @param object The bean instance. Use {@link #getPropertyType(Class, String)} to look up
     *        property types when an instance is not available.
     * @param propertyPath A valid property path expression in the context of the bean.
     * @return The property type referred to by the provided bean and property path.
     * @see ObjectPathExpressionParser
     */
    public static Class<?> getPropertyType(Object object, String propertyPath) {
        try {
            ObjectPropertyReference.setWarning(true);
            return ObjectPropertyReference.resolvePath(object, object.getClass(), propertyPath, false)
                    .getPropertyType();
        } finally {
            ObjectPropertyReference.setWarning(false);
        }
    }

    /**
     * Gets the property names by property type, based on the read methods.
     * 
     * @param bean The bean.
     * @param propertyType The return type of the read method on the property.
     * @return list of property names
     */
    public static Set<String> getReadablePropertyNamesByType(
            Object bean, Class<?> propertyType) {
        return getReadablePropertyNamesByType(bean.getClass(), propertyType);
    }

    /**
     * Gets the property names by property type, based on the read methods.
     * 
     * @param beanClass The bean class.
     * @param propertyType The return type of the read method on the property.
     * @return list of property names
     */
    public static Set<String> getReadablePropertyNamesByType(
            Class<?> beanClass, Class<?> propertyType) {
        return getMetadata(beanClass).getReadablePropertyNamesByType(propertyType);
    }

    /**
     * Gets the property names by annotation type, based on the read methods.
     * 
     * @param bean The bean.
     * @param annotationType The type of an annotation on the return type.
     * @return list of property names
     */
    public static Set<String> getReadablePropertyNamesByAnnotationType(
            Object bean, Class<? extends Annotation> annotationType) {
        return getReadablePropertyNamesByAnnotationType(bean.getClass(), annotationType);
    }

    /**
     * Gets the property names by annotation type, based on the read methods.
     * 
     * @param beanClass The bean class.
     * @param annotationType The type of an annotation on the return type.
     * @return list of property names
     */
    public static Set<String> getReadablePropertyNamesByAnnotationType(
            Class<?> beanClass, Class<? extends Annotation> annotationType) {
        return getMetadata(beanClass).getReadablePropertyNamesByAnnotationType(annotationType);
    }

    /**
     * Gets the property names by collection type, based on the read methods.
     * 
     * @param bean The bean.
     * @param collectionType The type of elements in a collection or array.
     * @return list of property names
     */
    public static Set<String> getReadablePropertyNamesByCollectionType(
            Object bean, Class<?> collectionType) {
        return getReadablePropertyNamesByCollectionType(bean.getClass(), collectionType);
    }

    /**
     * Gets the property names for the given object that are writable
     *
     * @param bean object to get writable property names for
     * @return set of property names
     */
    public static Set<String> getWritablePropertyNames(Object bean) {
        return getMetadata(bean.getClass()).getWritablePropertyNames();
    }

    /**
     * Gets the property names by collection type, based on the read methods.
     * 
     * @param beanClass The bean class.
     * @param collectionType The type of elements in a collection or array.
     * @return list of property names
     */
    public static Set<String> getReadablePropertyNamesByCollectionType(
            Class<?> beanClass, Class<?> collectionType) {
        return getMetadata(beanClass).getReadablePropertyNamesByCollectionType(collectionType);
    }

    /**
     * Look up a property value.
     * 
     * @param <T> property type
     * @param object The bean instance to look up a property value for.
     * @param propertyPath A valid property path expression in the context of the bean.
     * @return The value of the property referred to by the provided bean and property path.
     * @see ObjectPathExpressionParser
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T getPropertyValue(Object object, String propertyPath) {
        boolean trace = ProcessLogger.isTraceActive() && object != null; 
        if (trace) {
            // May be uncommented for debugging high execution count
            // ProcessLogger.ntrace(object.getClass().getSimpleName() + ":r:" + propertyPath, "", 1000);
            ProcessLogger.countBegin("bean-property-read");
        }

        try {
            ObjectPropertyReference.setWarning(true);

            return (T) ObjectPropertyReference.resolvePath(object, object.getClass(), propertyPath, false).get();

        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Error getting property '" + propertyPath + "' from " + object, e);
        } finally {
            ObjectPropertyReference.setWarning(false);
            if (trace) {
                ProcessLogger.countEnd("bean-property-read", object.getClass().getSimpleName() + ":" + propertyPath);
            }
        }

    }

    /**
     * Initialize a property value.
     * 
     * <p>
     * Upon returning from this method, the property referred to by the provided bean and property
     * path will have been initialized with a default instance of the indicated property type.
     * </p>
     * 
     * @param object The bean instance to initialize a property value for.
     * @param propertyPath A valid property path expression in the context of the bean.
     * @see #getPropertyType(Object, String)
     * @see #setPropertyValue(Object, String, Object)
     * @see ObjectPathExpressionParser
     */
    public static void initializeProperty(Object object, String propertyPath) {
        Class<?> propertyType = getPropertyType(object, propertyPath);
        try {
            setPropertyValue(object, propertyPath, propertyType.newInstance());
        } catch (InstantiationException e) {
            // just set the value to null
            setPropertyValue(object, propertyPath, null);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to set new instance for property: " + propertyPath, e);
        }
    }

    /**
     * Modify a property value.
     * 
     * <p>
     * Upon returning from this method, the property referred to by the provided bean and property
     * path will have been populated with property value provided. If the propertyValue does not
     * match the type of the indicated property, then type conversion will be attempted using
     * {@link PropertyEditorManager}.
     * </p>
     * 
     * @param object The bean instance to initialize a property value for.
     * @param propertyPath A valid property path expression in the context of the bean.
     * @param propertyValue The value to populate value in the property referred to by the provided
     *        bean and property path.
     * @see ObjectPathExpressionParser
     * @throws RuntimeException If the property path is not valid in the context of the bean
     *         provided.
     */
    public static void setPropertyValue(Object object, String propertyPath, Object propertyValue) {
        if (ProcessLogger.isTraceActive() && object != null) {
            // May be uncommented for debugging high execution count
            // ProcessLogger.ntrace(object.getClass().getSimpleName() + ":w:" + propertyPath + ":", "", 1000);
            ProcessLogger.countBegin("bean-property-write");
        }

        try {
            ObjectPropertyReference.setWarning(true);

            ObjectPropertyReference.resolvePath(object, object.getClass(), propertyPath, true).set(propertyValue);

        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Error setting property '" + propertyPath + "' on " + object + " with " + propertyValue, e);
        } finally {
            ObjectPropertyReference.setWarning(false);

            if (ProcessLogger.isTraceActive() && object != null) {
                ProcessLogger.countEnd("bean-property-write", object.getClass().getSimpleName() + ":" + propertyPath);
            }
        }

    }

    /**
     * Modify a property value.
     * 
     * <p>
     * Upon returning from this method, the property referred to by the provided bean and property
     * path will have been populated with property value provided. If the propertyValue does not
     * match the type of the indicated property, then type conversion will be attempted using
     * {@link PropertyEditorManager}.
     * </p>
     * 
     * @param object The bean instance to initialize a property value for.
     * @param propertyPath A property path expression in the context of the bean.
     * @param propertyValue The value to populate value in the property referred to by the provided
     *        bean and property path.
     * @param ignoreUnknown True if invalid property values should be ignored, false to throw a
     *        RuntimeException if the property reference is invalid.
     * @see ObjectPathExpressionParser
     */
    public static void setPropertyValue(Object object, String propertyPath, Object propertyValue, boolean ignoreUnknown) {
        try {
            setPropertyValue(object, propertyPath, propertyValue);
        } catch (RuntimeException e) {
            // only throw exception if they have indicated to not ignore unknown
            if (!ignoreUnknown) {
                throw e;
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace("Ignoring exception thrown during setting of property '" + propertyPath + "': "
                        + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Determine if a property is readable.
     * 
     * @param object The bean instance to initialize a property value for.
     * @param propertyPath A property path expression in the context of the bean.
     * @return True if the path expression resolves to a valid readable property reference in the
     *         context of the bean provided.
     */
    public static boolean isReadableProperty(Object object, String propertyPath) {
        return ObjectPropertyReference.resolvePath(object, object.getClass(), propertyPath, false).canRead();
    }

    /**
     * Determine if a property is writable.
     * 
     * @param object The bean instance to initialize a property value for.
     * @param propertyPath A property path expression in the context of the bean.
     * @return True if the path expression resolves to a valid writable property reference in the
     *         context of the bean provided.
     */
    public static boolean isWritableProperty(Object object, String propertyPath) {
        return ObjectPropertyReference.resolvePath(object, object.getClass(), propertyPath, false).canWrite();
    }

    /**
     * Returns an List of {@code Field} objects reflecting all the fields
     * declared by the class or interface represented by this
     * {@code Class} object. This includes public, protected, default
     * (package) access, and private fields, and includes inherited fields.
     *
     * @param fields A list of {@code Field} objects which gets returned.
     * @param type Type of class or interface for which fields are returned.
     * @param stopAt The Superclass upto which the inherited fields are to be included
     * @return List of all fields
     */
    public static List<Field> getAllFields(List<Field> fields, Class<?> type, Class<?> stopAt) {
        for (Field field : type.getDeclaredFields()) {
            fields.add(field);
        }

        if (type.getSuperclass() != null && !type.getName().equals(stopAt.getName())) {
            fields = getAllFields(fields, type.getSuperclass(), stopAt);
        }

        return fields;
    }

    /**
     * Get the best known component type for a generic type.
     * 
     * <p>
     * When the type is not parameterized or has no explicitly defined parameters, {@link Object} is
     * returned.
     * </p>
     * 
     * <p>
     * When the type has multiple parameters, the right-most parameter is considered the component
     * type. This facilitates identifying the value type of a Map.
     * </p>
     * 
     * @param type The generic collection or map type.
     * @return component or value type, resolved from the generic type
     */
    public static Type getComponentType(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return Object.class;
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] params = parameterizedType.getActualTypeArguments();
        if (params.length == 0) {
            return Object.class;
        }

        Type valueType = params[params.length - 1];
        return valueType;
    }

    /**
     * Get the upper bound of a generic type.
     * 
     * <p>
     * When the type is a class, the class is returned.
     * </p>
     * 
     * <p>
     * When the type is a wildcard, and the upper bound is a class, the upper bound of the wildcard
     * is returned.
     * </p>
     * 
     * <p>
     * If the type has not been explicitly defined at compile time, {@link Object} is returned.
     * </p>
     * 
     * @param valueType The generic collection or map type.
     * @return component or value type, resolved from the generic type
     */
    public static Class<?> getUpperBound(Type valueType) {
        if (valueType instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) valueType).getUpperBounds();

            if (upperBounds.length >= 1) {
                valueType = upperBounds[0];
            }
        }

        if (valueType instanceof ParameterizedType) {
            valueType = ((ParameterizedType) valueType).getRawType();
        }

        if (valueType instanceof Class) {
            return (Class<?>) valueType;
        }

        return Object.class;
    }

    /**
     * Locate the generic type declaration for a given target class in the generic type hierarchy of
     * the source class.
     * 
     * @param sourceClass The class representing the generic type hierarchy to scan.
     * @param targetClass The class representing the generic type declaration to locate within the
     *        source class' hierarchy.
     * @return The generic type representing the target class within the source class' generic
     *         hierarchy.
     */
    public static Type findGenericType(Class<?> sourceClass, Class<?> targetClass) {
        if (!targetClass.isAssignableFrom(sourceClass)) {
            throw new IllegalArgumentException(targetClass + " is not assignable from " + sourceClass);
        }

        if (sourceClass.equals(targetClass)) {
            return sourceClass;
        }
        
        @SuppressWarnings("unchecked")
        Queue<Type> typeQueue = RecycleUtils.getInstance(LinkedList.class);
        typeQueue.offer(sourceClass);
        while (!typeQueue.isEmpty()) {
            Type type = typeQueue.poll();
            
            Class<?> upperBound = getUpperBound(type);
            if (targetClass.equals(upperBound)) {
                return type;
            }

            Type genericSuper = upperBound.getGenericSuperclass();
            if (genericSuper != null) {
                typeQueue.offer(genericSuper);
            }

            Type[] genericInterfaces = upperBound.getGenericInterfaces();
            for (int i=0; i<genericInterfaces.length; i++) {
                if (genericInterfaces[i] != null) {
                    typeQueue.offer(genericInterfaces[i]);
                }
            }
        }
        
        throw new IllegalStateException(targetClass + " is assignable from " + sourceClass
                + " but could not be found in the generic type hierarchy");
    }

    /**
     * Split parse path entry for supporting {@link ObjectPropertyUtils#splitPropertyPath(String)}. 
     * 
     * @author Kuali Rice Team (rice.collab@kuali.org)
     */
    private static class SplitPropertyPathEntry implements PathEntry {

        /**
         * Invoked at each path separator on the path.
         * 
         * <p>
         * Note that {@link ObjectPathExpressionParser} strips quotes and brackets then treats
         * list/map references as property names. However
         * {@link ObjectPropertyUtils#splitPropertyPath(String)} expects that a list/map reference
         * will be part of the path expression, as a reference to a specific element in a list or
         * map related to the bean. Therefore, this method needs to rejoin these list/map references
         * before returning.
         * </p>
         * 
         * @param parentPath The portion of the path leading up to the current node.
         * @param node The list of property names in the path.
         * @param next The next property name being parsed.
         * 
         * {@inheritDoc}
         */
        @Override
        public List<String> parse(String parentPath, Object node, String next) {
            if (next == null) {
                return new ArrayList<String>();
            }

            @SuppressWarnings("unchecked")
            List<String> rv = (List<String>) node;
            // First node, or no path separator in parent path.
            if (rv.isEmpty()) {
                rv.add(next);
                return rv;
            }
            
            rejoinTrailingIndexReference(rv, parentPath);
            rv.add(next);
            
            return rv;
        }
    }
    
    private static final SplitPropertyPathEntry SPLIT_PROPERTY_PATH_ENTRY = new SplitPropertyPathEntry();
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Helper method for splitting property paths with bracketed index references.
     * 
     * <p>
     * Since the parser treats index references as separate tokens, they need to be rejoined in order
     * to be maintained as a single indexed property reference.  This method handles that operation.
     * </p>
     * 
     * @param tokenList The list of tokens being parsed.
     * @param path The portion of the path expression represented by the token list.
     */
    private static void rejoinTrailingIndexReference(List<String> tokenList, String path) {
        int lastIndex = tokenList.size() - 1;
        String lastToken = tokenList.get(lastIndex);
        String lastParentToken = path.substring(path.lastIndexOf('.') + 1);
        
        if (!lastToken.equals(lastParentToken) && lastIndex > 0) {

            // read back one more token and "concatenate" by
            // recreating the subexpression as a substring of
            // the parent path
            String prevToken = tokenList.get(--lastIndex);
            
            // parent path index of last prevToken.
            int iopt = path.lastIndexOf(prevToken, path.lastIndexOf(lastToken));
            
            String fullToken = path.substring(iopt);
            tokenList.remove(lastIndex); // remove first entry
            tokenList.set(lastIndex, fullToken); // replace send with concatenation
        }
    }
    
    /**
     * Splits the given property path into a string of property names that make up the path.
     *
     * @param path property path to split
     * @return string array of names, starting from the top parent
     * @see SplitPropertyPathEntry#parse(String, Object, String)
     */
    public static String[] splitPropertyPath(String path) {
        List<String> split = ObjectPathExpressionParser.parsePathExpression(null, path, SPLIT_PROPERTY_PATH_ENTRY);
        if (split == null || split.isEmpty()) {
            return EMPTY_STRING_ARRAY;
        }
        
        rejoinTrailingIndexReference(split, path);

        return split.toArray(new String[split.size()]);
    }
    
    /**
     * Private constructor - utility class only.
     */
    private ObjectPropertyUtils() {}

    /**
     * Infer the read method based on method name.
     * 
     * @param beanClass The bean class.
     * @param propertyName The property name.
     * @return The read method for the property.
     */
    private static Method getReadMethodByName(Class<?> beanClass, String propertyName) {

        try {
            return beanClass.getMethod("get" + Character.toUpperCase(propertyName.charAt(0))
                    + propertyName.substring(1));
        } catch (SecurityException e) {
            // Ignore
        } catch (NoSuchMethodException e) {
            // Ignore
        }

        try {
            Method readMethod = beanClass.getMethod("is"
                    + Character.toUpperCase(propertyName.charAt(0))
                    + propertyName.substring(1));
            
            if (readMethod.getReturnType() == Boolean.class
                    || readMethod.getReturnType() == Boolean.TYPE) {
                return readMethod;
            }
        } catch (SecurityException e) {
            // Ignore
        } catch (NoSuchMethodException e) {
            // Ignore
        }
        
        return null;
    }
    
    /**
     * Get the cached metadata for a bean class.
     * 
     * @param beanClass The bean class.
     * @return cached metadata for beanClass
     */
    private static ObjectPropertyMetadata getMetadata(Class<?> beanClass) {
        ObjectPropertyMetadata metadata = METADATA_CACHE.get(beanClass);

        if (metadata == null) {
            metadata = new ObjectPropertyMetadata(beanClass);
            METADATA_CACHE.put(beanClass, metadata);
        }

        return metadata;
    }
    
    /**
     * Stores property metadata related to a bean class, for reducing introspection and reflection
     * overhead.
     * 
     * @author Kuali Rice Team (rice.collab@kuali.org)
     */
    private static class ObjectPropertyMetadata {

        private final Map<String, PropertyDescriptor> propertyDescriptors;
        private final Map<String, Method> readMethods;
        private final Map<String, Method> writeMethods;
        private final Map<Class<?>, Set<String>> readablePropertyNamesByPropertyType =
                Collections.synchronizedMap(new WeakHashMap<Class<?>, Set<String>>());
        private final Map<Class<?>, Set<String>> readablePropertyNamesByAnnotationType =
                Collections.synchronizedMap(new WeakHashMap<Class<?>, Set<String>>());
        private final Map<Class<?>, Set<String>> readablePropertyNamesByCollectionType =
                Collections.synchronizedMap(new WeakHashMap<Class<?>, Set<String>>());
        
        /**
         * Gets the property names by type, based on the read methods.
         * 
         * @param propertyType The return type of the read method on the property.
         * @return list of property names
         */
        private Set<String> getReadablePropertyNamesByType(Class<?> propertyType) {
            Set<String> propertyNames = readablePropertyNamesByPropertyType.get(propertyType);
            if (propertyNames != null) {
                return propertyNames;
            }
            
            propertyNames = new LinkedHashSet<String>();
            for (Entry<String, Method> readMethodEntry : readMethods.entrySet()) {
                Method readMethod = readMethodEntry.getValue();
                if (readMethod != null && propertyType.isAssignableFrom(readMethod.getReturnType())) {
                    propertyNames.add(readMethodEntry.getKey());
                }
            }
            
            propertyNames = Collections.unmodifiableSet(propertyNames);
            readablePropertyNamesByPropertyType.put(propertyType, propertyNames);
            
            return propertyNames;
        }

        /**
         * Gets the property names by annotation type, based on the read methods.
         * 
         * @param annotationType The type of an annotation on the return type.
         * @return list of property names
         */
        private Set<String> getReadablePropertyNamesByAnnotationType(
                Class<? extends Annotation> annotationType) {
            Set<String> propertyNames = readablePropertyNamesByAnnotationType.get(annotationType);
            if (propertyNames != null) {
                return propertyNames;
            }
            
            propertyNames = new LinkedHashSet<String>();
            for (Entry<String, Method> readMethodEntry : readMethods.entrySet()) {
                Method readMethod = readMethodEntry.getValue();
                if (readMethod != null && readMethod.isAnnotationPresent(annotationType)) {
                    propertyNames.add(readMethodEntry.getKey());
                }
            }
            
            propertyNames = Collections.unmodifiableSet(propertyNames);
            readablePropertyNamesByPropertyType.put(annotationType, propertyNames);
            
            return propertyNames;
        }

        /**
         * Gets the property names by collection type, based on the read methods.
         * 
         * @param collectionType The type of elements in a collection or array.
         * @return list of property names
         */
        private Set<String> getReadablePropertyNamesByCollectionType(Class<?> collectionType) {
            Set<String> propertyNames = readablePropertyNamesByCollectionType.get(collectionType);
            if (propertyNames != null) {
                return propertyNames;
            }
            
            propertyNames = new LinkedHashSet<String>();
            for (Entry<String, Method> readMethodEntry : readMethods.entrySet()) {
                Method readMethod = readMethodEntry.getValue();
                if (readMethod == null) {
                    continue;
                }
                
                Class<?> propertyClass = readMethod.getReturnType();
                if (propertyClass.isArray() &&
                        collectionType.isAssignableFrom(propertyClass.getComponentType())) {
                    propertyNames.add(readMethodEntry.getKey());
                    continue;
                }
                
                boolean isCollection = Collection.class.isAssignableFrom(propertyClass);
                boolean isMap = Map.class.isAssignableFrom(propertyClass);
                if (!isCollection && !isMap) {
                    continue;
                }
                
                if (collectionType.equals(Object.class)) {
                    propertyNames.add(readMethodEntry.getKey());
                    continue;
                }
                
                Type propertyType = readMethodEntry.getValue().getGenericReturnType();
                if (propertyType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) propertyType;
                    Type valueType = parameterizedType.getActualTypeArguments()[isCollection ? 0 : 1];

                    if (valueType instanceof WildcardType) {
                        Type[] upperBounds = ((WildcardType) valueType).getUpperBounds(); 
                        
                        if (upperBounds.length >= 1) {
                            valueType = upperBounds[0];
                        }
                    }
                    
                    if (valueType instanceof Class &&
                            collectionType.isAssignableFrom((Class<?>) valueType)) {
                        propertyNames.add(readMethodEntry.getKey());
                    }
                }
            }
            
            propertyNames = Collections.unmodifiableSet(propertyNames);
            readablePropertyNamesByCollectionType.put(collectionType, propertyNames);
            
            return propertyNames;
        }

        /**
         * Gets the property names that are writable for the metadata class.
         *
         * @return set of writable property names
         */
        private Set<String> getWritablePropertyNames() {
            Set<String> writablePropertyNames = new HashSet<String>();

            for (Entry<String, Method> writeMethodEntry : writeMethods.entrySet()) {
                writablePropertyNames.add(writeMethodEntry.getKey());
            }

            return writablePropertyNames;
        }

        /**
         * Creates a new metadata wrapper for a bean class.
         * 
         * @param beanClass The bean class.
         */
        private ObjectPropertyMetadata(Class<?> beanClass) {
            BeanInfo beanInfo;
            try {
                beanInfo = Introspector.getBeanInfo(beanClass);
            } catch (IntrospectionException e) {
                LOG.warn(
                        "Bean Info not found for bean " + beanClass, e);
                beanInfo = null;
            }

            Map<String, PropertyDescriptor> mutablePropertyDescriptorMap = new LinkedHashMap<String, PropertyDescriptor>();
            Map<String, Method> mutableReadMethodMap = new LinkedHashMap<String, Method>();
            Map<String, Method> mutableWriteMethodMap = new LinkedHashMap<String, Method>();

            if (beanInfo != null) {
                for (PropertyDescriptor propertyDescriptor : beanInfo
                        .getPropertyDescriptors()) {
                    String propertyName = propertyDescriptor.getName();

                    mutablePropertyDescriptorMap.put(propertyName, propertyDescriptor);
                    Method readMethod = propertyDescriptor.getReadMethod();
                    if (readMethod == null) {
                        readMethod = getReadMethodByName(beanClass, propertyName);
                    }
                    mutableReadMethodMap.put(propertyName, readMethod);

                    Method writeMethod = propertyDescriptor.getWriteMethod();
                    assert writeMethod == null
                            || (writeMethod.getParameterTypes().length == 1 && writeMethod.getParameterTypes()[0] != null) : writeMethod;
                    mutableWriteMethodMap.put(propertyName, writeMethod);
                }
            }

            propertyDescriptors = Collections.unmodifiableMap(mutablePropertyDescriptorMap);
            readMethods = Collections.unmodifiableMap(mutableReadMethodMap);
            writeMethods = Collections.unmodifiableMap(mutableWriteMethodMap);
        }

    }
    
}
