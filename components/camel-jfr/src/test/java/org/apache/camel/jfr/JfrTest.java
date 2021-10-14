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
package org.apache.camel.jfr;

import java.util.HashSet;
import java.util.Set;

import org.apache.camel.main.Main;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.EnableEvent;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;

import static org.moditect.jfrunit.ExpectedEvent.event;
import static org.moditect.jfrunit.JfrEventsAssert.assertThat;

@JfrEventTest
public class JfrTest {

    public JfrEvents jfrEvents = new JfrEvents();

    @Test
    @EnableEvent("jdk.ObjectAllocationInNewTLAB")
    @EnableEvent("jdk.ObjectAllocationOutsideTLAB")
    @EnableEvent("jdk.ObjectCount")
    @EnableEvent("jdk.ClassLoad")
    public void shouldHaveGcAndSleepEvents() throws Exception {
        final Main main = new Main();
        main.configure().setDurationMaxMessages(1);
        // FIXME startupRecorderDir not working????
        main.configure().setStartupRecorderDir("target");

        main.configure().addRoutesBuilder(SimpleRouteBuilder.class);

        main.run();

        jfrEvents.awaitEvents();

        Set<String> events = new HashSet<>();
        jfrEvents.events().forEach(event -> {
            events.add(event.getEventType().getName());
        });

        /*
        jfrEvents.events().filter(event("jdk.ClassLoadingStatistics")).forEach(event -> {
            System.out.println(event.toString());
        });
        
        jfrEvents.events().filter(event("jdk.ObjectCount")).forEach(event -> {
            System.out.println(event.toString());
        });
        
        TODO some ClassLoad event can be useful?
        jfrEvents.events().filter(event("jdk.ClassLoad"))
            .filter(event -> event.getClass("loadedClass").getName().contains("org.apache.camel"))
            .forEach(event -> {
                System.out.println(event.toString());
        });
        */

        System.out.println("single events recorded");
        events.forEach(event -> System.out.println(event));

        assertThat(jfrEvents).contains(event("jdk.JavaThreadStatistics")
                .with("activeCount", 10L)
                .with("daemonCount", 8L)
                .with("peakCount", 13L));

        assertThat(jfrEvents).contains(event("jdk.ClassLoadingStatistics"));

        jfrEvents.events().filter(event("jdk.ClassLoadingStatistics")).forEach(event -> {
            Assertions.assertThat(event.getLong("loadedClassCount") < 4571L);
        });

        jfrEvents.events().filter(event("jdk.ObjectAllocationInNewTLAB")).forEach(event -> {
            Assertions.assertThat(event.getThread().getJavaName().endsWith("timer://foo"));
            Assertions.assertThat(event.getDouble("allocationSize") == (double) 104);
        });

        jfrEvents.events().filter(event("jdk.ObjectAllocationOutsideTLAB")).forEach(event -> {
            Assertions.assertThat(event.getThread().getJavaName().endsWith("timer://foo"));
            Assertions.assertThat(event.getDouble("allocationSize") == (double) 11.5);
        });
    }
}
