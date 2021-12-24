/**
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

package org.apache.camel.component.mllp;

import java.util.function.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.junit.rule.mllp.MllpServerResource;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.mllp.Hl7TestMessageGenerator;
import org.junit.Rule;
import org.junit.Test;

import static org.apache.camel.ExchangePattern.InOut;


public class LogPhiTest extends CamelTestSupport {

    static final int SERVER_ACKNOWLEDGEMENT_DELAY = 10000;

    @Rule
    public MllpServerResource mllpServer = new MllpServerResource();

    @EndpointInject(uri = "direct:startNoLogPhi")
    private Endpoint startNoLogPhi;

    @EndpointInject(uri = "direct:startLogPhi")
    private Endpoint startLogPhi;


    @Override
    protected void doPreSetup() throws Exception {
        mllpServer.setListenHost("localhost");
        mllpServer.setListenPort(AvailablePortFinder.getNextAvailable());
        mllpServer.setDelayDuringAcknowledgement(SERVER_ACKNOWLEDGEMENT_DELAY);
        mllpServer.startup();
        assertTrue(mllpServer.isActive());
        super.doPreSetup();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() {

                from(startNoLogPhi)
                        .toF("mllp://%s:%d?receiveTimeout=%d",
                                mllpServer.getListenHost(), mllpServer.getListenPort(),
                                SERVER_ACKNOWLEDGEMENT_DELAY / 2);

                from(startLogPhi)
                        .toF("mllp://%s:%d?receiveTimeout=%d",
                                mllpServer.getListenHost(), mllpServer.getListenPort(),
                                SERVER_ACKNOWLEDGEMENT_DELAY / 2);
            }
        };
    }


    @Test
    public void testLogPhiFalse() throws Exception {
        MllpComponent.setLogPhi(false);
        testLogPhi(startNoLogPhi, exceptionMessage -> assertFalse(exceptionMessage.contains("hl7Message")));
    }

    @Test
    public void testLogPhiTrue() throws Exception {
        MllpComponent.setLogPhi(true);
        testLogPhi(startLogPhi, exceptionMessage -> assertTrue(exceptionMessage.contains("hl7Message")));
    }


    public void testLogPhi(Endpoint endpoint, Consumer<String> contains) throws Exception {
        Exchange exchange = endpoint.createExchange(InOut);
        String message = Hl7TestMessageGenerator.generateMessage();
        exchange.getIn().setBody(message);
        assertEquals(ServiceStatus.Started, context.getStatus());
        Exchange out = template.send(endpoint, exchange);
        assertTrue("Should be failed", exchange.isFailed());
        contains.accept(exchange.getException().getMessage());
    }
}
