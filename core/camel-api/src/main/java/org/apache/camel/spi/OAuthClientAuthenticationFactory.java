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
package org.apache.camel.spi;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;

/**
 * Factory for creating a {@link Processor} that acquires an OAuth 2.0 bearer token using the client_credentials grant
 * and sets the {@code Authorization: Bearer <token>} header on the Exchange message.
 * <p/>
 * This requires camel-oauth on the classpath.
 */
public interface OAuthClientAuthenticationFactory {

    /**
     * Service factory key.
     */
    String FACTORY = "oauth-client-authentication-factory";

    /**
     * Creates a {@link Processor} using explicit configuration.
     * <p/>
     * The returned Processor, when invoked, will acquire a token from the configured token endpoint using the
     * client_credentials grant, cache the token if caching is enabled, and set the {@code Authorization: Bearer} header
     * on the Exchange's message.
     *
     * @param  config    the OAuth client configuration
     * @return           a Processor that sets the Authorization header
     * @throws Exception if the processor cannot be created
     */
    Processor createOAuthClientAuthenticationProcessor(OAuthClientConfig config) throws Exception;

    /**
     * Creates a {@link Processor} using a named profile resolved from Camel properties.
     * <p/>
     * Properties are resolved from {@code camel.oauth.<profileName>.*}:
     * <ul>
     * <li>{@code camel.oauth.<profileName>.client-id} (required)</li>
     * <li>{@code camel.oauth.<profileName>.client-secret} (required)</li>
     * <li>{@code camel.oauth.<profileName>.token-endpoint} (required)</li>
     * <li>{@code camel.oauth.<profileName>.scope} (optional)</li>
     * <li>{@code camel.oauth.<profileName>.cache-tokens} (optional, default true)</li>
     * <li>{@code camel.oauth.<profileName>.cached-tokens-default-expiry-seconds} (optional, default 3600)</li>
     * <li>{@code camel.oauth.<profileName>.cached-tokens-expiration-margin-seconds} (optional, default 5)</li>
     * </ul>
     *
     * @param  context     the CamelContext to resolve properties from
     * @param  profileName the named profile (e.g., "keycloak", "azure")
     * @return             a Processor that sets the Authorization header
     * @throws Exception   if required properties are missing or the processor cannot be created
     */
    Processor createOAuthClientAuthenticationProcessor(CamelContext context, String profileName) throws Exception;

    /**
     * Creates a {@link Processor} using the default (unnamed) profile.
     * <p/>
     * Properties are resolved from {@code camel.oauth.*} directly (backward compatible with existing single-IdP
     * configuration):
     * <ul>
     * <li>{@code camel.oauth.client-id}</li>
     * <li>{@code camel.oauth.client-secret}</li>
     * <li>{@code camel.oauth.token-endpoint}</li>
     * </ul>
     *
     * @param  context   the CamelContext to resolve properties from
     * @return           a Processor that sets the Authorization header
     * @throws Exception if required properties are missing or the processor cannot be created
     */
    Processor createOAuthClientAuthenticationProcessor(CamelContext context) throws Exception;
}
