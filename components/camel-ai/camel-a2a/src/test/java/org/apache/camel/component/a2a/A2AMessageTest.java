package org.apache.camel.component.a2a;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.a2a.A2A;
import io.a2a.client.A2AClient;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
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

public class A2AMessageTest {

    int port1 = AvailablePortFinder.getNextAvailable();
    int port2 = AvailablePortFinder.getNextAvailable();

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

                from("a2a:Hello World Agent?agentCard=#agentCard&port=" + port1)
                        .routeId("hello-world-agent")
                        .setBody()
                        .simple("Hello Camel from ${routeId} with message ${body}");

                AgentCapabilities agentCapabilities = new AgentCapabilities.Builder()
                        .streaming(false)
                        .pushNotifications(false)
                        .stateTransitionHistory(false).build();

                getContext().getRegistry().bind("agentCapabilities", agentCapabilities);

                AgentSkill agentSkill = new AgentSkill.Builder()
                        .id("another_greet")
                        .name("Returns another greet")
                        .description("just returns another greet")
                        .tags(Collections.singletonList("hi"))
                        .examples(List.of("hi", "hello world"))
                        .build();

                getContext().getRegistry().bind("skills", List.of(agentSkill));

                from("a2a:Second Agent?" +
                     "description=Just another example&" +
                     "url=http://localhost:" + port2 + "&" +
                     "version=1.0.0&" +
                     "documentationUrl=http://example.com/docs&" +
                     "capabilities=#agentCapabilities&" +
                     "defaultInputModes=text&" +
                     "defaultOutputModes=text&" +
                     "skills=#skills&" +
                     "protocolVersion=0.2.5&" +
                     "port=" + port2)
                        .routeId("second-agent")
                        .setBody()
                        .simple("Hello Camel from ${routeId} with message ${body}");
            }
        });
    }

    @Test
    public void synchronousMessage() throws Exception {
        // Test route with agent card bean
        testAgent(port1, "greet me", "Hello Camel from hello-world-agent with message greet me",
                "Hello World Agent");

        // Test route with agent card endpoint
        testAgent(port2, "greet me again", "Hello Camel from second-agent with message greet me again",
                "Second Agent");
    }

    private void testAgent(int port, String greet, String expectedMessage, String expectedAgentName)
            throws Exception {
        A2AClient client = new A2AClient("http://localhost:" + port);

        Message message = A2A.toUserMessage(greet);
        MessageSendParams params = new MessageSendParams.Builder()
                .message(message)
                .build();
        SendMessageResponse response = client.sendMessage(params);

        Message result = (Message) response.getResult();
        TextPart textPart = (TextPart) result.getParts().get(0);

        Assertions.assertThat(textPart.getText())
                .isEqualTo(expectedMessage);

        AgentCard agentCard = client.getAgentCard();

        Assertions.assertThat(agentCard.name()).isEqualTo(expectedAgentName);
    }
}
