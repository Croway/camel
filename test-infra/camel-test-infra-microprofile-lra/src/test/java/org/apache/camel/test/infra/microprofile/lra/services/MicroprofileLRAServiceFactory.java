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
package org.apache.camel.test.infra.microprofile.lra.services;

import org.apache.camel.test.infra.common.services.SimpleTestServiceBuilder;
import org.apache.camel.test.infra.common.services.SingletonService;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class MicroprofileLRAServiceFactory {

    static class SingletonMicroprofileLRAService extends SingletonService<MicroprofileLRAService>
            implements MicroprofileLRAService {
        public SingletonMicroprofileLRAService(MicroprofileLRAService service, String name) {
            super(service, name);
        }

        @Override
        public String host() {
            return getService().host();
        }

        @Override
        public int port() {
            return getService().port();
        }

        @Override
        public String callbackHost() {
            return getService().callbackHost();
        }

        @Override
        public String getServiceAddress() {
            return getService().getServiceAddress();
        }

        @Override
        public void beforeAll(ExtensionContext extensionContext) {
            addToStore(extensionContext);
        }

        @Override
        public void afterAll(ExtensionContext extensionContext) {
            // NO-OP
        }
    }

    private static SimpleTestServiceBuilder<MicroprofileLRAService> instance;
    private static MicroprofileLRAService service;

    private MicroprofileLRAServiceFactory() {

    }

    public static SimpleTestServiceBuilder<MicroprofileLRAService> builder() {
        return new SimpleTestServiceBuilder<>("microprofile-lra");
    }

    public static MicroprofileLRAService createService() {
        return builder()
                .addLocalMapping(MicroprofileLRALocalContainerService::new)
                .addRemoteMapping(MicroprofileLRARemoteService::new)
                .build();
    }

    public static synchronized MicroprofileLRAService createSingletonService() {
        if (service == null) {
            if (instance == null) {
                instance = builder();

                instance.addLocalMapping(() -> new SingletonMicroprofileLRAService(
                        new MicroprofileLRALocalContainerService(), "microprofile-lra"))
                        .addRemoteMapping(MicroprofileLRARemoteService::new);
            }

            service = instance.build();
        }

        return service;
    }
}
