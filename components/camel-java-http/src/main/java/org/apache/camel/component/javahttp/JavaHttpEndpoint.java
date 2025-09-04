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

import java.net.http.HttpClient;

import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.LineNumberAware;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.http.common.HttpCommonEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send requests to external HTTP servers using JDK HTTP Client.
 */
@UriEndpoint(firstVersion = "4.15.0", scheme = "java-http,java-https", title = "Java HTTP,Java HTTPS",
             syntax = "java-http://httpUri",
             producerOnly = true, category = { Category.HTTP }, lenientProperties = true,
             headersClass = JavaHttpConstants.class)
@Metadata(excludeProperties = "httpBinding,matchOnUriPrefix,chunked,transferException", annotations = {
        "protocol=http"
})
@ManagedResource(description = "Managed JavaHttpEndpoint")
public class JavaHttpEndpoint extends HttpCommonEndpoint implements LineNumberAware {

    private static final Logger LOG = LoggerFactory.getLogger(JavaHttpEndpoint.class);

    private int lineNumber;
    private String location;

    @UriParam(label = "security", description = "To configure security using SSLContextParameters."
                                                + " Important: Only one instance of org.apache.camel.util.jsse.SSLContextParameters is supported per JavaHttpComponent."
                                                + " If you need to use 2 or more different instances, you need to define a new JavaHttpComponent per instance you need.")
    protected SSLContextParameters sslContextParameters;

    @UriParam(label = "timeout", defaultValue = "30000",
              description = "Determines the timeout (in millis) until a new connection is fully established."
                            + " A timeout value of zero is interpreted as an infinite timeout.")
    protected long connectTimeout = 30000L;

    @UriParam(label = "timeout", defaultValue = "30000",
              description = "Determines the default timeout (in millis) for HTTP requests.")
    protected long requestTimeout = 30000L;

    @UriParam(label = "producer,advanced", defaultValue = "8192",
              description = "This threshold in bytes controls whether the response payload"
                            + " should be stored in memory as a byte array or be streaming based. Set this to -1 to always use streaming mode.")
    protected int responsePayloadStreamingThreshold = 8192;

    @UriParam(label = "producer",
              description = "Whether to skip Camel control headers (CamelHttp... headers) to influence this endpoint. Control headers from previous HTTP components can influence"
                            + " how this Camel component behaves such as CamelHttpPath, CamelHttpQuery, etc.")
    private boolean skipControlHeaders;

    @UriParam(label = "producer",
              description = "Whether to skip mapping all the Camel headers as HTTP request headers."
                            + " This is useful when you know that calling the HTTP service should not include any custom headers.")
    protected boolean skipRequestHeaders;

    @UriParam(label = "producer",
              description = "Whether to skip mapping all the HTTP response headers to Camel headers.")
    protected boolean skipResponseHeaders;

    @UriParam(label = "producer,advanced",
              defaultValue = "true",
              description = "If this option is true then IN exchange headers will be copied to OUT exchange headers according to copy strategy."
                            + " Setting this to false, allows to only include the headers from the HTTP response (not propagating IN headers).")
    protected boolean copyHeaders = true;

    @UriParam(label = "producer,advanced", description = "To set a custom HTTP User-Agent request header")
    protected String userAgent;

    private final HttpClient httpClient;

    public JavaHttpEndpoint(String uri, JavaHttpComponent component, HttpClient httpClient) {
        super(uri, component, null);
        this.httpClient = httpClient;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new JavaHttpProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("Consumer not supported for Java HTTP component");
    }

    @Override
    public PollingConsumer createPollingConsumer() throws Exception {
        throw new UnsupportedOperationException("PollingConsumer not supported for Java HTTP component");
    }

    @Override
    public boolean isLenientProperties() {
        return true;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    @ManagedAttribute(description = "Connect timeout in milliseconds")
    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @ManagedAttribute(description = "Request timeout in milliseconds")
    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public SSLContextParameters getSslContextParameters() {
        return sslContextParameters;
    }

    public void setSslContextParameters(SSLContextParameters sslContextParameters) {
        this.sslContextParameters = sslContextParameters;
    }

    public int getResponsePayloadStreamingThreshold() {
        return responsePayloadStreamingThreshold;
    }

    public void setResponsePayloadStreamingThreshold(int responsePayloadStreamingThreshold) {
        this.responsePayloadStreamingThreshold = responsePayloadStreamingThreshold;
    }

    public boolean isSkipControlHeaders() {
        return skipControlHeaders;
    }

    public void setSkipControlHeaders(boolean skipControlHeaders) {
        this.skipControlHeaders = skipControlHeaders;
    }

    public boolean isSkipRequestHeaders() {
        return skipRequestHeaders;
    }

    public void setSkipRequestHeaders(boolean skipRequestHeaders) {
        this.skipRequestHeaders = skipRequestHeaders;
    }

    public boolean isSkipResponseHeaders() {
        return skipResponseHeaders;
    }

    public void setSkipResponseHeaders(boolean skipResponseHeaders) {
        this.skipResponseHeaders = skipResponseHeaders;
    }

    public boolean isCopyHeaders() {
        return copyHeaders;
    }

    public void setCopyHeaders(boolean copyHeaders) {
        this.copyHeaders = copyHeaders;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }
}
