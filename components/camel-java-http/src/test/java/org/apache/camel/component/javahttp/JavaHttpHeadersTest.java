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

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaHttpHeadersTest extends JavaHttpTestBase {

    @Test
    public void testHttpHeadersPropagation() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"args\": {}, \"headers\": {\"TestHeader\": \"test-value\", \"User-Agent\": \"MyCustomAgent/1.0\"}, \"origin\": \"127.0.0.1\", \"url\": \""
                                  + baseUrl + "/get\"}")));

        MockEndpoint mockEndpoint = getMockEndpoint("mock:result");
        mockEndpoint.expectedMessageCount(1);

        template.requestBodyAndHeaders("direct:start", "test body", Map.of(
                "TestHeader", "test-value",
                "User-Agent", "MyCustomAgent/1.0",
                "Accept", "application/json"));

        mockEndpoint.assertIsSatisfied();

        Exchange exchange = mockEndpoint.getReceivedExchanges().get(0);
        Message message = exchange.getIn();

        // Check response headers are populated
        assertNotNull(message.getHeader("Content-Type"));
        assertNotNull(message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Test
    public void testSkipRequestHeaders() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"args\": {}, \"headers\": {}, \"origin\": \"127.0.0.1\", \"url\": \"" + baseUrl
                                  + "/get\"}")));

        MockEndpoint mockEndpoint = getMockEndpoint("mock:result2");
        mockEndpoint.expectedMessageCount(1);

        template.requestBodyAndHeaders("direct:skipHeaders", "test body", Map.of(
                "TestHeader", "test-value",
                "User-Agent", "MyCustomAgent/1.0"));

        mockEndpoint.assertIsSatisfied();
    }

    @Test
    public void testSkipResponseHeaders() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"args\": {}, \"headers\": {}, \"origin\": \"127.0.0.1\", \"url\": \"" + baseUrl
                                  + "/get\"}")));

        MockEndpoint mockEndpoint = getMockEndpoint("mock:result3");
        mockEndpoint.expectedMessageCount(1);

        template.sendBody("direct:skipResponseHeaders", "test body");

        mockEndpoint.assertIsSatisfied();

        Exchange exchange = mockEndpoint.getReceivedExchanges().get(0);
        Message message = exchange.getIn();

        // Should still have response code but fewer headers
        assertNotNull(message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .to("java-http://" + getWireMockHost() + "/get?httpMethod=GET")
                        .to("mock:result");

                from("direct:skipHeaders")
                        .to("java-http://" + getWireMockHost() + "/get?httpMethod=GET&skipRequestHeaders=true")
                        .to("mock:result2");

                from("direct:skipResponseHeaders")
                        .to("java-http://" + getWireMockHost() + "/get?httpMethod=GET&skipResponseHeaders=true")
                        .to("mock:result3");
            }
        };
    }
}
