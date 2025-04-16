/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.support.component;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.NoSuchBeanException;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.support.EndpointHelper;
import org.apache.camel.util.TimeUtils;

/**
 * Base class used by Camel Package Maven Plugin when it generates source code for fast property configurations via
 * {@link org.apache.camel.spi.PropertyConfigurer}.
 */
public abstract class PropertyConfigurerSupport {

    /**
     * A special magic value that are used by tooling such as camel-jbang export
     */
    public static final String MAGIC_VALUE = "@@CamelMagicValue@@";

    /**
     * Converts the property to the expected type
     *
     * @param  camelContext the camel context
     * @param  type         the expected type
     * @param  value        the value
     * @return              the value converted to the expected type
     */
    public static <T> T property(CamelContext camelContext, Class<T> type, Object value) {
        Object convertedValue = value; // Use intermediate variable

        // If the type is not string based and the value is a string, check for bean reference or time pattern
        if (value instanceof String text && String.class != type) {
            if (EndpointHelper.isReferenceParameter(text)) {
                Object obj;
                // Special handling for List type to resolve potentially multiple beans
                if (type == List.class) {
                    obj = EndpointHelper.resolveReferenceListParameter(camelContext, text, Object.class);
                } else {
                    obj = EndpointHelper.resolveReferenceParameter(camelContext, text, type);
                }
                if (obj == null) {
                    // No bean found, throw an exception
                    throw new NoSuchBeanException(text, type.getName());
                }
                convertedValue = obj; // Assign resolved bean
            } else if (type == long.class || type == Long.class || type == int.class || type == Integer.class) {
                // Attempt to convert string to milliseconds if it's a time pattern (e.g., "5s")
                try {
                    long num = TimeUtils.toMilliSeconds(text);
                    // Cast to int if necessary
                    Object obj = (type == int.class || type == Integer.class) ? (int) num : num;
                    convertedValue = obj; // Assign converted time value
                } catch (IllegalArgumentException e) {
                    // Ignore exception, let the standard type converter handle it later if it's not a time pattern
                }
            }
        }

        // Special handling for boolean target type
        if (type == Boolean.class || type == boolean.class) {
            // Check if the value (potentially resolved bean or converted time) is a String
            if (convertedValue instanceof String text) {
                if (MAGIC_VALUE.equals(text)) {
                    // If the input was MAGIC_VALUE, treat it as "true" for boolean conversion
                    convertedValue = "true";
                } else if (!text.equalsIgnoreCase("true") && !text.equalsIgnoreCase("false")) {
                    // If it's a string but not "true", "false", or MAGIC_VALUE, throw error
                    throw new IllegalArgumentException(
                            "Cannot convert the String value: " + text + " to type: " + type
                                                       + " as the value is not true or false");
                }
                // If text is "true" or "false" (case-insensitive), let the converter handle it
            }
            // If convertedValue is not a String (e.g., already a Boolean or a resolved bean),
            // let mandatoryConvertTo handle the conversion (it might fail if bean isn't convertible).
        }

        // Perform the final mandatory type conversion
        if (convertedValue != null) {
            try {
                // The check for MAGIC_VALUE is removed from here as it's handled above
                return camelContext.getTypeConverter().mandatoryConvertTo(type, convertedValue);
            } catch (NoTypeConversionAvailableException e) {
                // Wrap and rethrow conversion exceptions
                throw RuntimeCamelException.wrapRuntimeCamelException(e);
            }
        } else {
            // Return null if the input value was null
            return null;
        }
    }

}
