package org.apache.camel.component.a2a;

import io.a2a.A2A;
import io.a2a.client.A2AClient;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageResponse;
import io.a2a.spec.TextPart;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.infra.core.CamelContextExtension;
import org.apache.camel.test.infra.core.DefaultCamelContextExtension;
import org.apache.camel.test.infra.core.annotations.RouteFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class A2ATaskTest {
    int port1 = AvailablePortFinder.getNextAvailable();

    @RegisterExtension
    protected static CamelContextExtension contextExtension = new DefaultCamelContextExtension();

    @RouteFixture
    public void createRouteBuilder(CamelContext context) throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                AgentCard agentCard = new AgentCard.Builder()
                        .name("Hello World Agent")
                        .description("Just a hello world agent")
                        .url("http://localhost:" + port1)
                        .version("1.0.0")
                        .documentationUrl("http://example.com/docs")
                        .capabilities(new AgentCapabilities.Builder()
                                .streaming(false)
                                .pushNotifications(true)
                                .stateTransitionHistory(true)
                                .build())
                        .defaultInputModes(Collections.singletonList("text"))
                        .defaultOutputModes(Collections.singletonList("text"))
                        .skills(Collections.singletonList(new AgentSkill.Builder()
                                .id("hello_world")
                                .name("Returns hello world")
                                .description("just returns hello world")
                                .tags(Collections.singletonList("hello world"))
                                .examples(List.of("hi", "hello world"))
                                .build()))
                        .protocolVersion("0.2.5")
                        .build();

                getContext().getRegistry().bind("agentCard", agentCard);

                from("a2a:Hello World Agent?agentCard=#agentCard&port=" + port1 + "&task=true")
                        .routeId("hello-world-agent")
                        .process(exchange -> {
                            // Long task
                            Thread.sleep(1000);
                        })
                        .setBody()
                        .simple("Hello Camel from ${routeId} with message ${body}");
            }
        });
    }

    @Test
    public void taskMessage() throws Exception {
        // Test route with agent card bean
        A2AClient client = new A2AClient("http://localhost:" + port1);

        MessageSendParams params = new MessageSendParams.Builder()
                .message(A2A.toAgentMessage("some text"))
                .build();
        SendMessageResponse response = client.sendMessage(params);

        // Assertion on response
        Message result = (Message) response.getResult();
        TextPart textPart = (TextPart) result.getParts().get(0);

        Assertions.assertThat(textPart.getText()).isNotNull();

        // Then GetTaskResponse getTaskResponse = client.getTask();
        GetTaskResponse getTaskResponse = client.getTask(textPart.getText());

        // assertions on getTaskResponse
        System.out.println(getTaskResponse);
    }
}
