/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.jsonapi;

import javax.annotation.processing.Generated;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.GeneratedPropertyConfigurer;
import org.apache.camel.support.component.PropertyConfigurerSupport;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.PackageDataFormatMojo")
@SuppressWarnings("unchecked")
public class JsonApiDataFormatConfigurer extends PropertyConfigurerSupport implements GeneratedPropertyConfigurer {

    @Override
    public boolean configure(CamelContext camelContext, Object target, String name, Object value, boolean ignoreCase) {
        JsonApiDataFormat dataformat = (JsonApiDataFormat) target;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "dataformattypes":
        case "dataFormatTypes": dataformat.setDataFormatTypes(property(camelContext, java.lang.String.class, value)); return true;
        case "mainformattype":
        case "mainFormatType": dataformat.setMainFormatType(property(camelContext, java.lang.String.class, value)); return true;
        default: return false;
        }
    }

}

