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
package org.apache.camel.test.infra.artemis.services;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.camel.test.AvailablePortFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractArtemisEmbeddedService implements ArtemisService {

    protected int tcpPort = AvailablePortFinder.getNextAvailable();

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractArtemisEmbeddedService.class);

    protected EmbeddedActiveMQ embeddedBrokerService;

    private Configuration artemisConfiguration;

    public AbstractArtemisEmbeddedService() {
        embeddedBrokerService = new EmbeddedActiveMQ();

        // Base configuration
        artemisConfiguration = new ConfigurationImpl();
        artemisConfiguration.setSecurityEnabled(false);
        artemisConfiguration.setBrokerInstance(new File("target/artemis"));
        artemisConfiguration.setJMXManagementEnabled(false);

        embeddedBrokerService.setConfiguration(getConfiguration(artemisConfiguration, AvailablePortFinder.getNextAvailable()));
    }

    protected abstract Configuration getConfiguration(Configuration artemisConfiguration, int port);

    @Override
    public String userName() {
        return null;
    }

    @Override
    public String password() {
        return null;
    }

    @Override
    public void restart() {

    }

    @Override
    public void initialize() {
        try {
            embeddedBrokerService.start();
            embeddedBrokerService.getActiveMQServer().waitForActivation(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.warn("Unable to start embedded Artemis broker: {}", e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        try {
            embeddedBrokerService.stop();
        } catch (Exception e) {
            LOG.warn("Unable to start embedded Artemis broker: {}", e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    public EmbeddedActiveMQ getEmbeddedBrokerService() {
        return embeddedBrokerService;
    }
}
