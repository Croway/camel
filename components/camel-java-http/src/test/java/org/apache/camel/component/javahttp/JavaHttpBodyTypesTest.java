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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaHttpBodyTypesTest extends JavaHttpTestBase {

    @Test
    public void testStringBody() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"data\": \"Hello World\", \"headers\": {\"Content-Type\": \"text/plain\"}}")));

        Exchange exchange = template.request("direct:stringBody", exchange1 -> {
            exchange1.getIn().setBody("Hello World");
            exchange1.getIn().setHeader("Content-Type", "text/plain");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Test
    public void testByteArrayBody() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"data\": \"Hello Bytes\", \"headers\": {\"Content-Type\": \"application/octet-stream\"}}")));

        Exchange exchange = template.request("direct:byteArrayBody", exchange1 -> {
            exchange1.getIn().setBody("Hello Bytes".getBytes());
            exchange1.getIn().setHeader("Content-Type", "application/octet-stream");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Test
    public void testInputStreamBody() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"data\": \"Hello Stream\", \"headers\": {\"Content-Type\": \"application/octet-stream\"}}")));

        Exchange exchange = template.request("direct:inputStreamBody", exchange1 -> {
            InputStream is = new ByteArrayInputStream("Hello Stream".getBytes());
            exchange1.getIn().setBody(is);
            exchange1.getIn().setHeader("Content-Type", "application/octet-stream");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Test
    public void testJsonBody() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withRequestBody(containing("Hello JSON"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"json\": {\"message\": \"Hello JSON\", \"number\": 42}, \"headers\": {\"Content-Type\": \"application/json\"}}")));

        Exchange exchange = template.request("direct:jsonBody", exchange1 -> {
            exchange1.getIn().setBody("{\"message\":\"Hello JSON\",\"number\":42}");
            exchange1.getIn().setHeader("Content-Type", "application/json");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        // Verify the JSON was echoed back in the response
        String response = message.getBody(String.class);
        assertNotNull(response);
    }

    @Test
    public void testEmptyBody() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"args\": {}, \"headers\": {}, \"origin\": \"127.0.0.1\", \"url\": \"" + baseUrl
                                  + "/get\"}")));

        Exchange exchange = template.request("direct:emptyBody", exchange1 -> {
            exchange1.getIn().setBody(null);
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:stringBody")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:byteArrayBody")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:inputStreamBody")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:jsonBody")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:emptyBody")
                        .to("java-http://" + getWireMockHost() + "/get?httpMethod=GET");
            }
        };
    }
}
