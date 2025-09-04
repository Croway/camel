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

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Producer;
import org.apache.camel.SSLContextParametersAware;
import org.apache.camel.http.base.HttpHelper;
import org.apache.camel.http.common.HttpBinding;
import org.apache.camel.http.common.HttpCommonComponent;
import org.apache.camel.http.common.HttpRestHeaderFilterStrategy;
import org.apache.camel.spi.BeanIntrospection;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.RestConfiguration;
import org.apache.camel.spi.RestProducerFactory;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.CamelContextHelper;
import org.apache.camel.support.PluginHelper;
import org.apache.camel.support.RestProducerFactoryHelper;
import org.apache.camel.support.http.HttpUtil;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.service.ServiceHelper;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;
import org.apache.camel.util.URISupport;
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the Java HTTP Component using JDK HTTP Client
 */
@Metadata(label = "verifiers", enums = "parameters,connectivity")
@Component("java-http,java-https")
public class JavaHttpComponent extends HttpCommonComponent implements RestProducerFactory, SSLContextParametersAware {

    private static final Logger LOG = LoggerFactory.getLogger(JavaHttpComponent.class);

    @Metadata(label = "security", description = "To configure security using SSLContextParameters."
                                                + " Important: Only one instance of org.apache.camel.support.jsse.SSLContextParameters is supported per JavaHttpComponent."
                                                + " If you need to use 2 or more different instances, you need to define a new JavaHttpComponent per instance you need.")
    protected SSLContextParameters sslContextParameters;

    // timeout
    @Metadata(label = "timeout", defaultValue = "" + 30000,
              description = "Determines the timeout (in millis) until a new connection is fully established."
                            + " A timeout value of zero is interpreted as an infinite timeout.")
    protected long connectTimeout = 30000L;
    @Metadata(label = "timeout", defaultValue = "" + 30000,
              description = "Determines the default timeout (in millis) for HTTP requests.")
    protected long requestTimeout = 30000L;

    // proxy
    @Metadata(label = "producer,proxy", description = "Proxy server host")
    protected String proxyHost;
    @Metadata(label = "producer,proxy", description = "Proxy server port")
    protected Integer proxyPort;
    @Metadata(label = "producer,proxy", secret = true, description = "Proxy server username")
    protected String proxyUsername;
    @Metadata(label = "producer,proxy", secret = true, description = "Proxy server password")
    protected String proxyPassword;

    @Metadata(label = "security", defaultValue = "false", description = "Enable usage of global SSL context parameters.")
    protected boolean useGlobalSslContextParameters;
    @Metadata(label = "producer,advanced", defaultValue = "8192",
              description = "This threshold in bytes controls whether the response payload"
                            + " should be stored in memory as a byte array or be streaming based. Set this to -1 to always use streaming mode.")
    protected int responsePayloadStreamingThreshold = 8192;
    @Metadata(label = "advanced", description = "Disables automatic redirect handling")
    protected boolean redirectHandlingDisabled;
    @Metadata(label = "producer",
              description = "Whether to skip Camel control headers (CamelHttp... headers) to influence this endpoint. Control headers from previous HTTP components can influence"
                            + " how this Camel component behaves such as CamelHttpPath, CamelHttpQuery, etc.")
    private boolean skipControlHeaders;
    @Metadata(label = "producer",
              description = "Whether to skip mapping all the Camel headers as HTTP request headers."
                            + " This is useful when you know that calling the HTTP service should not include any custom headers.")
    protected boolean skipRequestHeaders;
    @Metadata(label = "producer",
              description = "Whether to skip mapping all the HTTP response headers to Camel headers.")
    protected boolean skipResponseHeaders;
    @Metadata(label = "producer,advanced",
              defaultValue = "true",
              description = "If this option is true then IN exchange headers will be copied to OUT exchange headers according to copy strategy."
                            + " Setting this to false, allows to only include the headers from the HTTP response (not propagating IN headers).")
    protected boolean copyHeaders = true;
    @Metadata(label = "producer,advanced", defaultValue = "true",
              description = "Whether to the HTTP request should follow redirects."
                            + " By default the HTTP request follows redirects ")
    protected boolean followRedirects = true;
    @Metadata(label = "producer,advanced", description = "To set a custom HTTP User-Agent request header")
    protected String userAgent;

    public JavaHttpComponent() {
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Map<String, Object> httpClientParameters = new HashMap<>(parameters);
        final Map<String, Object> httpClientOptions = new HashMap<>();

        // timeout values can be configured on both component and endpoint level, where endpoint takes priority
        long valConnectTimeout = getAndRemoveParameter(parameters, "connectTimeout", long.class, connectTimeout);
        long valRequestTimeout = getAndRemoveParameter(parameters, "requestTimeout", long.class, requestTimeout);

        HttpBinding httpBinding = resolveAndRemoveReferenceParameter(parameters, "httpBinding", HttpBinding.class);

        SSLContextParameters sslContextParameters
                = resolveAndRemoveReferenceParameter(parameters, "sslContextParameters", SSLContextParameters.class);
        if (sslContextParameters == null) {
            sslContextParameters = getSslContextParameters();
        }
        if (sslContextParameters == null) {
            // only secure (https) should use global SSL
            boolean secure = HttpHelper.isSecureConnection(uri);
            if (secure) {
                sslContextParameters = retrieveGlobalSslContextParameters();
            }
        }

        String httpMethodRestrict = getAndRemoveParameter(parameters, "httpMethodRestrict", String.class);
        boolean muteException = getAndRemoveParameter(parameters, "muteException", boolean.class, isMuteException());

        HeaderFilterStrategy headerFilterStrategy
                = resolveAndRemoveReferenceParameter(parameters, "headerFilterStrategy", HeaderFilterStrategy.class);

        // the actual protocol if present in the remainder part should take precedence
        String secureProtocol = uri;
        if (remaining.startsWith("http:") || remaining.startsWith("https:")) {
            secureProtocol = remaining;
        }
        boolean secure = HttpHelper.isSecureConnection(secureProtocol) || sslContextParameters != null;

        // the remaining part should be without protocol as that was how this component was originally created
        remaining = removeHttpOrHttpsProtocol(remaining);

        // need to set the scheme on address uri depending on if it's secure or not
        String addressUri = (secure ? "https://" : "http://") + remaining;

        addressUri = UnsafeUriCharactersEncoder.encodeHttpURI(addressUri);
        URI uriHttpUriAddress = new URI(addressUri);

        // the endpoint uri should use the component name as the scheme, so we need to re-create it once more
        String scheme = StringHelper.before(uri, "://");

        // uri part should be without protocol as that was how this component was originally created
        uri = removeHttpOrHttpsProtocol(uri);

        URI endpointUri = URISupport.createRemainingURI(uriHttpUriAddress, httpClientParameters);

        endpointUri = URISupport.createRemainingURI(
                new URI(
                        scheme,
                        endpointUri.getUserInfo(),
                        endpointUri.getHost(),
                        endpointUri.getPort(),
                        endpointUri.getPath(),
                        endpointUri.getQuery(),
                        endpointUri.getFragment()),
                httpClientParameters);

        // create the endpoint and set the http uri to be null
        String endpointUriString = endpointUri.toString();

        LOG.debug("Creating endpoint uri {}", endpointUriString);

        // Create HttpClient with JDK HTTP Client
        HttpClient httpClient = createHttpClient(parameters, sslContextParameters, valConnectTimeout, valRequestTimeout);

        JavaHttpEndpoint endpoint = new JavaHttpEndpoint(endpointUriString, this, httpClient);
        endpoint.setConnectTimeout(valConnectTimeout);
        endpoint.setRequestTimeout(valRequestTimeout);
        endpoint.setCopyHeaders(copyHeaders);
        endpoint.setSkipControlHeaders(skipControlHeaders);
        endpoint.setSkipRequestHeaders(skipRequestHeaders);
        endpoint.setSkipResponseHeaders(skipResponseHeaders);
        endpoint.setUserAgent(userAgent);
        endpoint.setMuteException(muteException);

        // configure the endpoint with the common configuration from the component
        if (getHttpConfiguration() != null) {
            Map<String, Object> properties = new HashMap<>();
            BeanIntrospection beanIntrospection = PluginHelper.getBeanIntrospection(getCamelContext());
            beanIntrospection.getProperties(getHttpConfiguration(), properties, null);
            setProperties(endpoint, properties);
        }

        // configure the endpoint
        setProperties(endpoint, parameters);

        // we cannot change the port of an URI, we must create a new one with an explicit port value
        URI httpUri = URISupport.createRemainingURI(
                new URI(
                        uriHttpUriAddress.getScheme(),
                        uriHttpUriAddress.getUserInfo(),
                        uriHttpUriAddress.getHost(),
                        uriHttpUriAddress.getPort(),
                        uriHttpUriAddress.getPath(),
                        uriHttpUriAddress.getQuery(),
                        uriHttpUriAddress.getFragment()),
                parameters);

        endpoint.setHttpUri(httpUri);

        if (headerFilterStrategy != null) {
            endpoint.setHeaderFilterStrategy(headerFilterStrategy);
        } else {
            setEndpointHeaderFilterStrategy(endpoint);
        }
        endpoint.setHttpBinding(getHttpBinding());
        if (httpBinding != null) {
            endpoint.setHttpBinding(httpBinding);
        }
        if (httpMethodRestrict != null) {
            endpoint.setHttpMethodRestrict(httpMethodRestrict);
        }

        return endpoint;
    }

    protected HttpClient createHttpClient(
            final Map<String, Object> parameters,
            final SSLContextParameters sslContextParameters,
            long connectTimeout,
            long requestTimeout)
            throws Exception {

        HttpClient.Builder clientBuilder = HttpClient.newBuilder();

        // Set timeouts
        clientBuilder.connectTimeout(Duration.ofMillis(connectTimeout));

        // Configure SSL if needed
        if (sslContextParameters != null) {
            SSLContext sslContext = sslContextParameters.createSSLContext(getCamelContext());
            clientBuilder.sslContext(sslContext);
        }

        // Configure proxy if specified
        String proxyHost = getParameter(parameters, "proxyHost", String.class, getProxyHost());
        Integer proxyPort = getParameter(parameters, "proxyPort", Integer.class, getProxyPort());

        if (proxyHost != null && proxyPort != null) {
            java.net.ProxySelector proxySelector = java.net.ProxySelector.of(
                    new java.net.InetSocketAddress(proxyHost, proxyPort));
            clientBuilder.proxy(proxySelector);
        }

        // Configure redirect policy
        boolean followRedirects = getParameter(parameters, "followRedirects", Boolean.class, this.followRedirects);
        if (redirectHandlingDisabled || !followRedirects) {
            clientBuilder.followRedirects(HttpClient.Redirect.NEVER);
        } else {
            clientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
        }

        return clientBuilder.build();
    }

    protected String removeHttpOrHttpsProtocol(String uri) {
        if (uri.startsWith("http://")) {
            return uri.substring(7);
        } else if (uri.startsWith("https://")) {
            return uri.substring(8);
        }
        return uri;
    }

    @Override
    protected boolean useIntrospectionOnEndpoint() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Producer createProducer(
            CamelContext camelContext, String host,
            String verb, String basePath, String uriTemplate, String queryParameters,
            String consumes, String produces, RestConfiguration configuration, Map<String, Object> parameters)
            throws Exception {

        // avoid leading slash
        basePath = FileUtil.stripLeadingSeparator(basePath);
        uriTemplate = FileUtil.stripLeadingSeparator(uriTemplate);

        // get the endpoint
        String url = host;
        if (!ObjectHelper.isEmpty(basePath)) {
            url += "/" + basePath;
        }
        if (!ObjectHelper.isEmpty(uriTemplate)) {
            url += "/" + uriTemplate;
        }

        RestConfiguration config = configuration;
        if (config == null) {
            config = CamelContextHelper.getRestConfiguration(getCamelContext(), null, "java-http");
        }

        Map<String, Object> map = new HashMap<>();
        // build query string, and append any endpoint configuration properties
        if (config.getProducerComponent() == null || config.getProducerComponent().equals("java-http")) {
            // setup endpoint options
            map.put("httpMethod", verb);
            if (config.getEndpointProperties() != null && !config.getEndpointProperties().isEmpty()) {
                map.putAll(config.getEndpointProperties());
            }
        }

        url = HttpUtil.recreateUrl(map, url);

        parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();

        // there are cases where we might end up here without component being created beforehand
        // we need to abide by the component properties specified in the parameters when creating
        // the component, one such case is when we switch from "java-http" to "java-https" component name
        RestProducerFactoryHelper.setupComponentFor(url, camelContext, (Map<String, Object>) parameters.remove("component"));

        JavaHttpEndpoint endpoint = (JavaHttpEndpoint) camelContext.getEndpoint(url, parameters);

        String path = uriTemplate != null ? uriTemplate : basePath;

        HeaderFilterStrategy headerFilterStrategy
                = resolveAndRemoveReferenceParameter(parameters, "headerFilterStrategy", HeaderFilterStrategy.class);
        if (headerFilterStrategy != null) {
            endpoint.setHeaderFilterStrategy(headerFilterStrategy);
        } else {
            endpoint.setHeaderFilterStrategy(new HttpRestHeaderFilterStrategy(path, queryParameters));
        }
        // the endpoint must be started before creating the producer
        ServiceHelper.startService(endpoint);

        return endpoint.createProducer();
    }

    // Getters and setters
    public SSLContextParameters getSslContextParameters() {
        return sslContextParameters;
    }

    public void setSslContextParameters(SSLContextParameters sslContextParameters) {
        this.sslContextParameters = sslContextParameters;
    }

    @Override
    public boolean isUseGlobalSslContextParameters() {
        return this.useGlobalSslContextParameters;
    }

    @Override
    public void setUseGlobalSslContextParameters(boolean useGlobalSslContextParameters) {
        this.useGlobalSslContextParameters = useGlobalSslContextParameters;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public int getResponsePayloadStreamingThreshold() {
        return responsePayloadStreamingThreshold;
    }

    public void setResponsePayloadStreamingThreshold(int responsePayloadStreamingThreshold) {
        this.responsePayloadStreamingThreshold = responsePayloadStreamingThreshold;
    }

    public boolean isRedirectHandlingDisabled() {
        return redirectHandlingDisabled;
    }

    public void setRedirectHandlingDisabled(boolean redirectHandlingDisabled) {
        this.redirectHandlingDisabled = redirectHandlingDisabled;
    }

    public boolean isCopyHeaders() {
        return copyHeaders;
    }

    public void setCopyHeaders(boolean copyHeaders) {
        this.copyHeaders = copyHeaders;
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

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
