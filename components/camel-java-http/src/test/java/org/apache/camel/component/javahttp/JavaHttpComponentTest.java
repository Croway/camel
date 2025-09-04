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

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaHttpComponentTest extends CamelTestSupport {

    @Test
    public void testJavaHttpComponent() throws Exception {
        CamelContext context = context();
        Endpoint endpoint = context.getEndpoint("java-http://www.example.com");

        assertNotNull(endpoint);
        assertTrue(endpoint instanceof JavaHttpEndpoint);

        JavaHttpEndpoint httpEndpoint = (JavaHttpEndpoint) endpoint;
        assertNotNull(httpEndpoint.getHttpClient());
    }

    @Test
    public void testJavaHttpsComponent() throws Exception {
        CamelContext context = context();
        Endpoint endpoint = context.getEndpoint("java-https://www.example.com");

        assertNotNull(endpoint);
        assertTrue(endpoint instanceof JavaHttpEndpoint);

        JavaHttpEndpoint httpEndpoint = (JavaHttpEndpoint) endpoint;
        assertNotNull(httpEndpoint.getHttpClient());
    }

    @Test
    public void testJavaHttpComponentWithParameters() throws Exception {
        CamelContext context = context();
        Endpoint endpoint = context.getEndpoint("java-http://www.example.com?connectTimeout=5000&requestTimeout=10000");

        assertNotNull(endpoint);
        assertTrue(endpoint instanceof JavaHttpEndpoint);

        JavaHttpEndpoint httpEndpoint = (JavaHttpEndpoint) endpoint;
        assertNotNull(httpEndpoint.getHttpClient());
        // Note: timeout values are set during component creation, not directly accessible from endpoint
    }
}
