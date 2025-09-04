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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaHttpRedirectTest extends JavaHttpTestBase {

    @Test
    public void testFollowRedirects() throws Exception {
        // Setup redirect chain: /redirect/2 -> /redirect/1 -> /get
        wireMock.stubFor(get(urlPathEqualTo("/redirect/2"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", baseUrl + "/redirect/1")));

        wireMock.stubFor(get(urlPathEqualTo("/redirect/1"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", baseUrl + "/get")));

        wireMock.stubFor(get(urlPathEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"args\": {}, \"headers\": {}, \"origin\": \"127.0.0.1\", \"url\": \"" + baseUrl
                                  + "/get\"}")));

        Exchange exchange = template.request("direct:followRedirects", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
        assertNotNull(message.getBody(String.class));
    }

    @Test
    public void testDontFollowRedirects() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/redirect/1"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", baseUrl + "/get")));

        Exchange exchange = template.request("direct:dontFollowRedirects", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        // Should get redirect status code when not following redirects
        Object responseCode = message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE);
        assertNotNull(responseCode);
        // Should be 302 as we configured
        assertEquals(302, responseCode);
    }

    @Test
    public void testRelativeRedirect() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/relative-redirect/1"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", "/get")));

        wireMock.stubFor(get(urlPathEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"args\": {}, \"headers\": {}, \"origin\": \"127.0.0.1\", \"url\": \"" + baseUrl
                                  + "/get\"}")));

        Exchange exchange = template.request("direct:relativeRedirect", exchange1 -> {
            // Empty processor
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
                from("direct:followRedirects")
                        .to("java-http://" + getWireMockHost() + "/redirect/2?httpMethod=GET&followRedirects=true");

                from("direct:dontFollowRedirects")
                        .to("java-http://" + getWireMockHost()
                            + "/redirect/1?httpMethod=GET&followRedirects=false&throwExceptionOnFailure=false");

                from("direct:relativeRedirect")
                        .to("java-http://" + getWireMockHost() + "/relative-redirect/1?httpMethod=GET&followRedirects=true");
            }
        };
    }
}
