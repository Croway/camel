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
package org.apache.camel.component.a2a;

import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.EndpointServiceLocation;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultEndpoint;

@UriEndpoint(firstVersion = "4.14.0-SNAPSHOT", scheme = "a2a", title = "A2A", syntax = "a2a:agentName",
             category = { Category.AI })
public class A2AEndpoint extends DefaultEndpoint implements EndpointServiceLocation {

    @UriParam
    private A2AConfiguration configuration;

    @Metadata(required = true)
    @UriPath(description = "The Agent Name")
    private final String agentName;

    public A2AEndpoint(String uri, A2AComponent component, String agentName, A2AConfiguration configuration) {
        super(uri, component);
        this.configuration = configuration;
        this.agentName = agentName;
    }

    @Override
    public Producer createProducer() throws Exception {
        throw new UnsupportedOperationException("Producer not supported");
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return new A2AConsumer(this, processor, agentName);
    }

    public A2AConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String getServiceUrl() {
        return configuration.getUrl();
    }

    @Override
    public String getServiceProtocol() {
        return "a2a";
    }
}
