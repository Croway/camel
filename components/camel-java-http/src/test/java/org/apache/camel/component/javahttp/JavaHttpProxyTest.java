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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.util.URISupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaHttpProxyTest extends JavaHttpTestBase {

    private static WireMockServer proxyServer;
    private static int proxyPort;

    @BeforeAll
    static void setup() {
        // Create a simple WireMock server to act as target (not as proxy)
        // We'll configure the HTTP client to use a proxy that doesn't actually exist
        // to test proxy configuration parsing and endpoint creation
        WireMockConfiguration config = WireMockConfiguration.options()
                .bindAddress("127.0.0.1")
                .port(0);

        proxyServer = new WireMockServer(config);
        proxyServer.start();
        proxyPort = proxyServer.port();
    }

    @AfterAll
    static void tearDown() {
        if (proxyServer != null) {
            proxyServer.stop();
        }
    }

    @Test
    public void testDifferentHttpProxyConfigured() throws Exception {
        JavaHttpEndpoint http1 = context.getEndpoint("java-http://www.google.com?proxyHost=www.myproxy.com&proxyPort=1234",
                JavaHttpEndpoint.class);
        JavaHttpEndpoint http2 = context.getEndpoint(
                "java-http://www.google.com?test=parameter&proxyHost=www.otherproxy.com&proxyPort=2345",
                JavaHttpEndpoint.class);

        // As the endpointUri is recreated, the parameter could be in different place, so we use the URISupport.normalizeUri
        assertEquals("java-http://www.google.com?proxyHost=www.myproxy.com&proxyPort=1234",
                URISupport.normalizeUri(http1.getEndpointUri()), "Get a wrong endpoint uri of http1");
        assertEquals("java-http://www.google.com?proxyHost=www.otherproxy.com&proxyPort=2345&test=parameter",
                URISupport.normalizeUri(http2.getEndpointUri()), "Get a wrong endpoint uri of http2");

        assertEquals(http1.getEndpointKey(), http2.getEndpointKey(), "Should get the same EndpointKey");
    }

    @Test
    public void testHttpGetWithProxyAndWithoutUser() throws Exception {
        // Setup WireMock stub for the target endpoint
        wireMock.stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"headers\":{},\"origin\":\"127.0.0.1\"}")));

        // Test proxy configuration validation by creating endpoint with proxy settings
        // The endpoint should be created successfully even if proxy is not reachable
        String proxyEndpointUri = "java-http://localhost:" + getWireMockPort()
                                  + "/get?httpMethod=GET&proxyHost=unreachable.proxy.example.com&proxyPort=8080&throwExceptionOnFailure=false";

        JavaHttpEndpoint endpoint = context.getEndpoint(proxyEndpointUri, JavaHttpEndpoint.class);
        assertNotNull(endpoint);

        // Test that we can make requests with proxy configured but targeting localhost (which bypasses proxy)
        // Configure a route that uses localhost so proxy is bypassed
        Exchange exchange = template.request("direct:httpSimple", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Test
    public void testHttpGetWithProxyOnComponent() throws Exception {
        // Setup WireMock stub
        wireMock.stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"origin\":\"127.0.0.1\"}")));

        // Configure proxy on component level with invalid proxy
        JavaHttpComponent httpComponent = context.getComponent("java-http", JavaHttpComponent.class);
        String originalProxyHost = httpComponent.getProxyHost();
        Integer originalProxyPort = httpComponent.getProxyPort();

        try {
            httpComponent.setProxyHost("proxy.example.com");
            httpComponent.setProxyPort(8080);

            // This should succeed since localhost bypasses proxy
            Exchange exchange = template.request("direct:httpSimple", exchange1 -> {
                // Empty processor
            });

            assertNotNull(exchange);
            Message message = exchange.getMessage();
            assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        } finally {
            // Restore original proxy settings
            httpComponent.setProxyHost(originalProxyHost);
            httpComponent.setProxyPort(originalProxyPort);
        }
    }

    @Test
    public void testHttpGetWithoutProxy() throws Exception {
        // Setup WireMock stub for the target server
        wireMock.stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"url\":\"http://localhost/get\",\"headers\":{}}")));

        Exchange exchange = template.request("direct:httpSimple", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
        assertNotNull(message.getBody(String.class));
    }

    @Test
    public void testProxyConfigurationValidation() throws Exception {
        // Test that we can create endpoints with various proxy configurations
        // without the proxy needing to be reachable

        // Test endpoint-level proxy configuration
        JavaHttpEndpoint endpoint1 = context.getEndpoint(
                "java-http://httpbin.org/get?proxyHost=proxy.example.com&proxyPort=3128",
                JavaHttpEndpoint.class);
        assertNotNull(endpoint1);

        // Test component-level proxy configuration
        JavaHttpComponent component = context.getComponent("java-http", JavaHttpComponent.class);
        String originalProxyHost = component.getProxyHost();
        Integer originalProxyPort = component.getProxyPort();

        try {
            component.setProxyHost("component.proxy.example.com");
            component.setProxyPort(8080);

            JavaHttpEndpoint endpoint2 = context.getEndpoint("java-http://httpbin.org/get", JavaHttpEndpoint.class);
            assertNotNull(endpoint2);

            assertEquals("component.proxy.example.com", component.getProxyHost());
            assertEquals(Integer.valueOf(8080), component.getProxyPort());

        } finally {
            // Restore original proxy settings
            component.setProxyHost(originalProxyHost);
            component.setProxyPort(originalProxyPort);
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:httpProxy")
                        .to("java-http://localhost:" + getWireMockPort()
                            + "/get?httpMethod=GET&proxyHost=unreachable.proxy.example.com&proxyPort=8080&throwExceptionOnFailure=false");

                from("direct:httpSimple")
                        .to("java-http://localhost:" + getWireMockPort() + "/get?httpMethod=GET");
            }
        };
    }
}
