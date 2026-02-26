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
package org.apache.camel.oauth;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.JsonObject;
import org.apache.camel.Exchange;
import org.apache.camel.spi.OAuthClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.oauth.OAuth.CAMEL_OAUTH_CLIENT_ID;
import static org.apache.camel.oauth.OAuth.CAMEL_OAUTH_CLIENT_SECRET;
import static org.apache.camel.oauth.OAuthProperties.getRequiredProperty;

public class OAuthClientCredentialsProcessor extends AbstractOAuthProcessor {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Thread-safe token cache for direct config mode. Keyed by (tokenEndpoint, clientId).
     */
    private static final ConcurrentMap<TokenCacheKey, UserProfile> TOKEN_CACHE = new ConcurrentHashMap<>();

    private final OAuthClientConfig clientConfig;

    /**
     * Default constructor - uses the full OAuth/OIDC infrastructure with global camel.oauth.* properties.
     */
    public OAuthClientCredentialsProcessor() {
        this.clientConfig = null;
    }

    /**
     * Constructor with explicit config - uses direct token endpoint call with caching. Supports multiple IdPs.
     */
    public OAuthClientCredentialsProcessor(OAuthClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public void process(Exchange exchange) {
        if (clientConfig != null) {
            processWithDirectConfig(exchange);
        } else {
            processWithOAuthInfrastructure(exchange);
        }
    }

    /**
     * Direct config mode: POST to token endpoint, cache result, set Authorization header. Lightweight, no OIDC
     * discovery or session management.
     */
    private void processWithDirectConfig(Exchange exchange) {
        UserProfile profile;

        if (clientConfig.isCacheTokens()) {
            TokenCacheKey cacheKey = new TokenCacheKey(clientConfig.getTokenEndpoint(), clientConfig.getClientId());
            long margin = clientConfig.getCachedTokensExpirationMarginSeconds();
            profile = TOKEN_CACHE.compute(cacheKey, (key, existing) -> {
                if (existing != null && existing.ttl() > margin) {
                    return existing;
                }
                return acquireToken();
            });
        } else {
            profile = acquireToken();
        }

        String accessToken = profile.accessToken()
                .orElseThrow(() -> new OAuthException("No access_token in token response"));
        exchange.getMessage().setHeader("Authorization", "Bearer " + accessToken);
    }

    /**
     * Full OIDC infrastructure mode: uses OAuthFactory, session management, and global properties. This is the original
     * behavior.
     */
    private void processWithOAuthInfrastructure(Exchange exchange) {
        var context = exchange.getContext();
        var msg = exchange.getMessage();

        logRequestHeaders(procName, msg);

        // Find or create the OAuth instance
        var oauth = findOAuth(context).orElseGet(() -> {
            var factory = OAuthFactory.lookupFactory(context);
            return factory.createOAuth();
        });

        // Get or create the OAuthSession
        var session = oauth.getOrCreateSession(exchange);

        // Authenticate an existing UserProfile from the OAuthSession
        if (session.getUserProfile().isPresent()) {
            var userProfile = session.removeUserProfile().orElseThrow();
            try {
                userProfile = authenticateExistingUserProfile(oauth, userProfile);
                session.putUserProfile(userProfile);
                return;
            } catch (OAuthException ex) {
                log.error("Failed to authenticate: {}", userProfile.subject(), ex);
            }
        }

        // Fallback to client credential grant
        var clientId = getRequiredProperty(exchange.getContext(), CAMEL_OAUTH_CLIENT_ID);
        var clientSecret = getRequiredProperty(exchange.getContext(), CAMEL_OAUTH_CLIENT_SECRET);

        var userProfile = oauth.authenticate(new ClientCredentials()
                .setClientSecret(clientSecret)
                .setClientId(clientId));

        session.putUserProfile(userProfile);
        log.info("Authenticated {}", userProfile.subject());
        userProfile.logDetails();

        // Add Authorization: Bearer <access-token>
        var accessToken = userProfile.accessToken().orElseThrow(() -> new OAuthException("No access_token"));
        msg.setHeader("Authorization", "Bearer " + accessToken);
    }

    private UserProfile acquireToken() {
        JsonObject json = OAuthTokenRequest.clientCredentialsGrant(
                clientConfig.getTokenEndpoint(),
                clientConfig.getClientId(),
                clientConfig.getClientSecret(),
                clientConfig.getScope());

        UserProfile profile = UserProfile.fromTokenResponse(json);
        log.debug("Acquired OAuth token from {}, ttl {}s", clientConfig.getTokenEndpoint(), profile.ttl());
        return profile;
    }

    record TokenCacheKey(String tokenEndpoint, String clientId) {
    }
}
