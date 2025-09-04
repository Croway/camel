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
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class JavaHttpTestBase extends CamelTestSupport {

    protected WireMockServer wireMock;
    protected String baseUrl;
    protected int wireMockPort;

    @BeforeEach
    public void setupWireMock() {
        if (wireMock == null) {
            wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
            wireMock.start();
            baseUrl = wireMock.baseUrl();
            wireMockPort = wireMock.port();
        }
    }

    @AfterEach
    public void tearDownWireMock() {
        if (wireMock != null) {
            wireMock.stop();
            wireMock = null;
        }
    }

    protected String getWireMockBaseUrl() {
        return baseUrl;
    }

    protected int getWireMockPort() {
        return wireMockPort;
    }

    protected String getWireMockHost() {
        return "localhost:" + wireMockPort;
    }

    @Override
    protected void doPreSetup() throws Exception {
        setupWireMock(); // Ensure WireMock is started before Camel setup
        super.doPreSetup();
    }

    @Override
    protected void doPostTearDown() throws Exception {
        tearDownWireMock();
        super.doPostTearDown();
    }
}
