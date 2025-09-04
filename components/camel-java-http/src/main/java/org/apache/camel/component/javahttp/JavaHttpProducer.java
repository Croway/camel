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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.LineNumberAware;
import org.apache.camel.Message;
import org.apache.camel.TypeConverter;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.http.common.HttpProtocolHeaderFilterStrategy;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.support.MessageHelper;
import org.apache.camel.support.http.HttpUtil;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.URISupport;
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaHttpProducer extends DefaultProducer implements LineNumberAware {

    private static final Logger LOG = LoggerFactory.getLogger(JavaHttpProducer.class);

    private static final Integer OK_RESPONSE_CODE = 200;

    private final HttpClient httpClient;
    private final boolean throwException;
    private final boolean transferException;
    private final HeaderFilterStrategy httpProtocolHeaderFilterStrategy = new HttpProtocolHeaderFilterStrategy();
    private int minOkRange = 200;
    private int maxOkRange = 299;
    private String defaultUrl;
    private URI defaultUri;

    public JavaHttpProducer(JavaHttpEndpoint endpoint) {
        super(endpoint);
        this.httpClient = endpoint.getHttpClient();
        this.throwException = endpoint.isThrowExceptionOnFailure();
        this.transferException = endpoint.isTransferException();
    }

    @Override
    public JavaHttpEndpoint getEndpoint() {
        return (JavaHttpEndpoint) super.getEndpoint();
    }

    @Override
    protected void doInit() throws Exception {
        super.doInit();

        String range = getEndpoint().getOkStatusCodeRange();
        parseStatusRange(range);

        // optimize and build default url when there are no override headers
        String url = getEndpoint().getHttpUri().toASCIIString();
        url = UnsafeUriCharactersEncoder.encodeHttpURI(url);
        URI uri = new URI(url);
        String queryString = getEndpoint().getHttpUri().getRawQuery();
        if (queryString == null) {
            queryString = uri.getRawQuery();
        }
        if (queryString != null) {
            queryString = UnsafeUriCharactersEncoder.encodeHttpURI(queryString);
            uri = URISupport.createURIWithQuery(uri, queryString);
        }
        defaultUri = uri;
        defaultUrl = uri.toASCIIString();
    }

    private void parseStatusRange(String range) {
        if (range.contains(",")) {
            // multiple ranges like "200-299,300-399"
            String[] ranges = range.split(",");
            for (String r : ranges) {
                if (!HttpUtil.parseStatusRange(r.trim(), this::setRanges)) {
                    int status = Integer.parseInt(r.trim());
                    setRanges(status, status);
                }
            }
        } else {
            // single range like "200-299" or "200"
            if (!HttpUtil.parseStatusRange(range, this::setRanges)) {
                int status = Integer.parseInt(range);
                setRanges(status, status);
            }
        }
    }

    private void setRanges(int minOkRange, int maxOkRange) {
        this.minOkRange = minOkRange;
        this.maxOkRange = maxOkRange;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        // determine the url to use for the HTTP request
        String url = determineUrl(exchange);
        URI uri = new URI(url);

        // create http request
        HttpRequest httpRequest = createHttpRequest(exchange, uri);

        LOG.debug("Executing HTTP request: {}", httpRequest);

        try {
            // execute the request
            HttpResponse<InputStream> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            LOG.debug("HTTP response code: {}", httpResponse.statusCode());

            // handle the response
            handleResponse(exchange, httpRequest, httpResponse);
        } catch (java.net.http.HttpTimeoutException e) {
            LOG.trace(e.getMessage(), e);
            if (throwException) {
                if (e instanceof HttpConnectTimeoutException connectTimeoutException) {
                    throw new HttpOperationFailedException(url, 0, "Connection timeout", null, null, null);
                } else {
                    throw new HttpOperationFailedException(url, 0, "Request timeout", null, null, null);
                }
            } else {
                // Set exchange properties to indicate timeout without throwing exception
                exchange.getMessage().setHeader(JavaHttpConstants.HTTP_RESPONSE_CODE, 0);
                exchange.setException(e);
            }
        } catch (java.net.ConnectException e) {
            LOG.trace(e.getMessage(), e);
            if (throwException) {
                throw new HttpOperationFailedException(url, 0, "Connection timeout", null, null, null);
            } else {
                // Set exchange properties to indicate connection failure without throwing exception
                exchange.getMessage().setHeader(JavaHttpConstants.HTTP_RESPONSE_CODE, 0);
                exchange.setException(e);
            }
        }
    }

    protected String determineUrl(Exchange exchange) throws URISyntaxException {
        String url = defaultUrl;

        // header takes precedence over URI parameters
        Object value = exchange.getIn().getHeader(JavaHttpConstants.HTTP_URI);
        if (value != null) {
            url = exchange.getContext().getTypeConverter().convertTo(String.class, value);
        }

        value = exchange.getIn().getHeader(JavaHttpConstants.HTTP_PATH);
        if (value != null) {
            String path = exchange.getContext().getTypeConverter().convertTo(String.class, value);
            if (ObjectHelper.isNotEmpty(path)) {
                URI baseUri = new URI(url);
                String existingPath = baseUri.getPath();
                String combinedPath;
                if (ObjectHelper.isEmpty(existingPath)) {
                    combinedPath = path;
                } else {
                    combinedPath = existingPath.endsWith("/") || path.startsWith("/")
                            ? existingPath + path
                            : existingPath + "/" + path;
                }
                url = new URI(
                        baseUri.getScheme(), baseUri.getUserInfo(), baseUri.getHost(),
                        baseUri.getPort(), combinedPath, baseUri.getQuery(), baseUri.getFragment()).toString();
            }
        }

        value = exchange.getIn().getHeader(JavaHttpConstants.HTTP_QUERY);
        if (value != null) {
            String query = exchange.getContext().getTypeConverter().convertTo(String.class, value);
            if (ObjectHelper.isNotEmpty(query)) {
                URI baseUri = new URI(url);
                url = new URI(
                        baseUri.getScheme(), baseUri.getUserInfo(), baseUri.getHost(),
                        baseUri.getPort(), baseUri.getPath(), query, baseUri.getFragment()).toString();
            }
        }

        return url;
    }

    protected HttpRequest createHttpRequest(Exchange exchange, URI uri) throws Exception {
        String method = determineHttpMethod(exchange);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(getEndpoint().getRequestTimeout()));

        // set headers
        populateHeaders(exchange, requestBuilder);

        // set body for non-GET methods
        if (!"GET".equals(method) && !"HEAD".equals(method)) {
            HttpRequest.BodyPublisher bodyPublisher = createBodyPublisher(exchange);
            requestBuilder.method(method, bodyPublisher);
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        return requestBuilder.build();
    }

    protected String determineHttpMethod(Exchange exchange) {
        String method = exchange.getIn().getHeader(JavaHttpConstants.HTTP_METHOD, String.class);
        if (method == null) {
            org.apache.camel.http.common.HttpMethods httpMethod = getEndpoint().getHttpMethod();
            if (httpMethod != null) {
                method = httpMethod.name();
            }
        }
        if (method == null) {
            method = "GET";
        }
        return method.toUpperCase();
    }

    protected void populateHeaders(Exchange exchange, HttpRequest.Builder requestBuilder) {
        Message in = exchange.getIn();
        HeaderFilterStrategy strategy = getEndpoint().getHeaderFilterStrategy();

        // propagate headers from IN message
        Map<String, Object> headers = in.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                // convert to string and add header
                String headerValue = exchange.getContext().getTypeConverter().convertTo(String.class, value);
                if (headerValue != null && strategy != null
                        && !strategy.applyFilterToExternalHeaders(name, headerValue, exchange)) {
                    requestBuilder.header(name, headerValue);
                }
            }
        }

        // set user agent if configured
        String userAgent = getEndpoint().getUserAgent();
        if (userAgent != null) {
            requestBuilder.header("User-Agent", userAgent);
        }

        // set content type if not already set and we have a body
        if (!"GET".equals(determineHttpMethod(exchange)) && !"HEAD".equals(determineHttpMethod(exchange))) {
            if (!headers.containsKey("Content-Type")) {
                requestBuilder.header("Content-Type", "application/octet-stream");
            }
        }
    }

    protected HttpRequest.BodyPublisher createBodyPublisher(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        Object body = in.getBody();

        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }

        TypeConverter tc = exchange.getContext().getTypeConverter();

        // try to convert to string first
        String text = tc.tryConvertTo(String.class, exchange, body);
        if (text != null) {
            return HttpRequest.BodyPublishers.ofString(text, StandardCharsets.UTF_8);
        }

        // try to convert to byte array
        byte[] data = tc.tryConvertTo(byte[].class, exchange, body);
        if (data != null) {
            return HttpRequest.BodyPublishers.ofByteArray(data);
        }

        // try to convert to input stream
        InputStream is = tc.tryConvertTo(InputStream.class, exchange, body);
        if (is != null) {
            // Read the input stream into byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOHelper.copy(is, baos);
            return HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray());
        }

        // fallback to string conversion
        String fallback = tc.mandatoryConvertTo(String.class, exchange, body);
        return HttpRequest.BodyPublishers.ofString(fallback, StandardCharsets.UTF_8);
    }

    protected void handleResponse(Exchange exchange, HttpRequest httpRequest, HttpResponse<InputStream> httpResponse)
            throws Exception {

        int responseCode = httpResponse.statusCode();

        LOG.debug("HTTP response code: {}", responseCode);

        // set response code on message
        Message message = exchange.getMessage();
        message.setHeader(JavaHttpConstants.HTTP_RESPONSE_CODE, responseCode);

        // set response headers
        if (!getEndpoint().isSkipResponseHeaders()) {
            populateResponseHeaders(exchange, httpResponse);
        }

        // extract and set response body
        extractResponseBody(exchange, httpResponse);

        // copy headers from IN to message if enabled
        if (getEndpoint().isCopyHeaders()) {
            MessageHelper.copyHeaders(exchange.getIn(), message, false);
        }

        // check for failure based on status code
        if (!isStatusCodeOk(responseCode)) {
            if (throwException) {
                String reason = httpResponse.headers().firstValue("reason-phrase").orElse("HTTP operation failed");
                throw new HttpOperationFailedException(
                        httpRequest.uri().toString(), responseCode, reason, null, null,
                        extractResponseBodyAsString(httpResponse));
            }
        }
    }

    protected boolean isStatusCodeOk(int statusCode) {
        return statusCode >= minOkRange && statusCode <= maxOkRange;
    }

    protected void populateResponseHeaders(Exchange exchange, HttpResponse<InputStream> httpResponse) {
        Message message = exchange.getMessage();
        HeaderFilterStrategy strategy = getEndpoint().getHeaderFilterStrategy();

        httpResponse.headers().map().forEach((name, values) -> {
            for (String value : values) {
                if (strategy != null && !strategy.applyFilterToExternalHeaders(name, value, exchange)) {
                    message.setHeader(name, value);
                }
            }
        });
    }

    protected void extractResponseBody(Exchange exchange, HttpResponse<InputStream> httpResponse) throws IOException {
        InputStream responseBody = httpResponse.body();
        Message message = exchange.getMessage();

        if (responseBody == null) {
            return;
        }

        int threshold = getEndpoint().getResponsePayloadStreamingThreshold();

        if (threshold > 0) {
            // try to determine content length
            int contentLength = httpResponse.headers().firstValue("Content-Length")
                    .map(Integer::parseInt).orElse(-1);

            if (contentLength > 0 && contentLength <= threshold) {
                // small response, read into memory
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOHelper.copy(responseBody, baos);
                message.setBody(baos.toByteArray());
            } else {
                // large response or unknown size, use streaming
                message.setBody(responseBody);
            }
        } else {
            // streaming disabled, always read into memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOHelper.copy(responseBody, baos);
            message.setBody(baos.toByteArray());
        }
    }

    protected String extractResponseBodyAsString(HttpResponse<InputStream> httpResponse) throws IOException {
        InputStream responseBody = httpResponse.body();
        if (responseBody == null) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOHelper.copy(responseBody, baos);
        return baos.toString(StandardCharsets.UTF_8);
    }

    @Override
    public int getLineNumber() {
        if (getEndpoint() instanceof LineNumberAware) {
            return ((LineNumberAware) getEndpoint()).getLineNumber();
        }
        return -1;
    }

    @Override
    public void setLineNumber(int lineNumber) {
        if (getEndpoint() instanceof LineNumberAware) {
            ((LineNumberAware) getEndpoint()).setLineNumber(lineNumber);
        }
    }

    @Override
    public String getLocation() {
        if (getEndpoint() instanceof LineNumberAware) {
            return ((LineNumberAware) getEndpoint()).getLocation();
        }
        return null;
    }

    @Override
    public void setLocation(String location) {
        if (getEndpoint() instanceof LineNumberAware) {
            ((LineNumberAware) getEndpoint()).setLocation(location);
        }
    }
}
