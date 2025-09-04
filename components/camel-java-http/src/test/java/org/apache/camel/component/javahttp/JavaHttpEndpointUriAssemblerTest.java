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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.EndpointUriFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaHttpEndpointUriAssemblerTest {

    @Test
    public void testJavaHttpAssembler() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("httpUri", "www.google.com");
        params.put("proxyHost", "myproxy");
        params.put("proxyPort", 8080);
        params.put("connectTimeout", 5000);
        params.put("requestTimeout", 10000);

        // should find the source code generated assembler via classpath
        try (CamelContext context = new DefaultCamelContext()) {
            context.start();

            EndpointUriFactory assembler = context.getCamelContextExtension().getEndpointUriFactory("java-https");

            assertNotNull(assembler);
            assertInstanceOf(JavaHttpEndpointUriFactory.class, assembler);

            String uri = assembler.buildUri("java-https", params);
            assertNotNull(uri);
            assertEquals(
                    "java-https://www.google.com?connectTimeout=5000&proxyHost=myproxy&proxyPort=8080&requestTimeout=10000",
                    uri);
        }
    }

    @Test
    public void testJavaHttpsAssembler() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("httpUri", "httpbin.org/get");
        params.put("followRedirects", false);
        params.put("userAgent", "MyCamelApp/1.0");

        try (CamelContext context = new DefaultCamelContext()) {
            context.start();

            EndpointUriFactory assembler = context.getCamelContextExtension().getEndpointUriFactory("java-http");

            assertNotNull(assembler);
            assertInstanceOf(JavaHttpEndpointUriFactory.class, assembler);

            String uri = assembler.buildUri("java-http", params);
            assertNotNull(uri);
            assertEquals(
                    "java-http://httpbin.org/get?followRedirects=false&userAgent=MyCamelApp%2F1.0",
                    uri);
        }
    }
}
