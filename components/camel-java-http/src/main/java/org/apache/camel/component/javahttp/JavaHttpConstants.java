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
package org.apache.camel.component.javahttp;

import org.apache.camel.spi.Metadata;

/**
 * Constants for Java HTTP component
 */
public final class JavaHttpConstants {

    @Metadata(description = "URI to call. Will override existing URI set directly on the endpoint.",
              javaType = "String")
    public static final String HTTP_URI = "CamelHttpUri";
    @Metadata(description = "Request URI's path", javaType = "String")
    public static final String HTTP_PATH = "CamelHttpPath";
    @Metadata(description = "URI parameters. Will override existing URI parameters set directly on the endpoint.",
              javaType = "String")
    public static final String HTTP_QUERY = "CamelHttpQuery";
    @Metadata(description = "How to set the Host header. Possible values are: default, none, custom", javaType = "String")
    public static final String HTTP_HOST_SETTING = "CamelHttpHostSetting";
    @Metadata(description = "The HTTP response code from the external server.", javaType = "int")
    public static final String HTTP_RESPONSE_CODE = "CamelHttpResponseCode";
    @Metadata(description = "The HTTP response text from the external server.", javaType = "String")
    public static final String HTTP_RESPONSE_TEXT = "CamelHttpResponseText";
    @Metadata(description = "Character encoding.", javaType = "String")
    public static final String HTTP_CHARACTER_ENCODING = "CamelHttpCharacterEncoding";
    @Metadata(description = "The HTTP content type. Is set on both the IN and OUT message to provide a content type, such as text/html.",
              javaType = "String")
    public static final String CONTENT_TYPE = "Content-Type";
    @Metadata(description = "The HTTP content encoding. Is set on both the IN and OUT message to provide a content encoding, such as gzip.",
              javaType = "String")
    public static final String CONTENT_ENCODING = "Content-Encoding";
    @Metadata(description = "The raw query string of the request URI", javaType = "String")
    public static final String HTTP_RAW_QUERY = "CamelHttpRawQuery";
    @Metadata(description = "To override the HTTP method with the given value.", javaType = "String")
    public static final String HTTP_METHOD = "CamelHttpMethod";
    @Metadata(description = "The URL to use for HTTP basic authentication.", javaType = "String")
    public static final String HTTP_URL = "CamelHttpUrl";

    private JavaHttpConstants() {
        // utility class
    }
}
