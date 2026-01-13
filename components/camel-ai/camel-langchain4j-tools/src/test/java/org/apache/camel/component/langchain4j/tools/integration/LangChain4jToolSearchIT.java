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
package org.apache.camel.component.langchain4j.tools.integration;

import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.tools.LangChain4jTools;
import org.apache.camel.component.langchain4j.tools.LangChain4jToolsComponent;
import org.apache.camel.component.langchain4j.tools.ToolsHelper;
import org.apache.camel.component.langchain4j.tools.spec.SearchableToolRegistry;
import org.apache.camel.test.infra.ollama.services.OllamaService;
import org.apache.camel.test.infra.ollama.services.OllamaServiceFactory;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the Tool Search feature using a real LLM (Ollama) and the all-MiniLM-L6-v2 ONNX embedding model.
 * This test verifies that the LLM can discover and use tools that are not directly exposed via the
 * search_available_tools meta-tool.
 */
@DisabledIfSystemProperty(named = "ci.env.name", matches = ".*", disabledReason = "Requires too much network resources")
public class LangChain4jToolSearchIT extends CamelTestSupport {

    @RegisterExtension
    static OllamaService OLLAMA = OllamaServiceFactory.createSingletonService();

    protected ChatModel chatModel;
    protected EmbeddingModel embeddingModel;

    @Override
    protected void setupResources() throws Exception {
        super.setupResources();

        chatModel = ToolsHelper.createModel(OLLAMA);
        embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    }

    @AfterEach
    void cleanUp() {
        SearchableToolRegistry.getInstance().clear();
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();

        LangChain4jToolsComponent component
                = context.getComponent(LangChain4jTools.SCHEME, LangChain4jToolsComponent.class);

        component.getConfiguration().setChatModel(chatModel);
        component.getConfiguration().setEmbeddingModel(embeddingModel);

        return context;
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {

                from("direct:test")
                        .to("langchain4j-tools:searchTest?tags=database")
                        .log("response is: ${body}");

                // Searchable tool (exposed=false) - requires search to discover
                from("langchain4j-tools:getUserTool?tags=database&name=getUserById"
                     + "&description=Retrieve user information from the database by their unique user ID"
                     + "&exposed=false&parameter.userId=integer")
                        .process(exchange -> {
                            Integer userId = exchange.getMessage().getHeader("userId", Integer.class);
                            exchange.getMessage().setBody(
                                    "{\"userId\": " + userId + ", \"name\": \"John Doe\", \"email\": \"john@example.com\"}");
                        });

                // Another searchable tool
                from("langchain4j-tools:getOrderTool?tags=database&name=getOrderHistory"
                     + "&description=Get purchase order history and transactions for a customer"
                     + "&exposed=false&parameter.customerId=integer")
                        .process(exchange -> {
                            Integer customerId = exchange.getMessage().getHeader("customerId", Integer.class);
                            exchange.getMessage().setBody(
                                    "{\"customerId\": " + customerId + ", \"orders\": [{\"id\": 101, \"total\": 99.99}]}");
                        });
            }
        };
    }

    @Test
    public void testToolSearchAndDiscovery() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(
                """
                        You are a helpful assistant with access to various tools.
                        If you don't see a tool that can help with a request, use the search_available_tools function
                        to discover available tools by describing what capability you need.
                        Once you discover a tool, you can use it to complete the task.
                        """));
        messages.add(new UserMessage("""
                What is the information for user with ID 42?
                """));

        Exchange exchange = fluentTemplate.to("direct:test").withBody(messages).request(Exchange.class);

        assertThat(exchange).isNotNull();
        assertThat(exchange.getMessage().getBody(String.class)).isNotNull();
        // The LLM should have discovered the getUserById tool via search and used it
        // The response should contain information about user 42
        assertThat(exchange.getMessage().getBody(String.class)).containsIgnoringCase("42");
    }

    @Test
    public void testSearchForOrderHistory() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(
                """
                        You are a helpful assistant with access to various tools.
                        Use search_available_tools to find tools that can help with the user's request.
                        """));
        messages.add(new UserMessage("""
                Show me the order history for customer 123.
                """));

        Exchange exchange = fluentTemplate.to("direct:test").withBody(messages).request(Exchange.class);

        assertThat(exchange).isNotNull();
        assertThat(exchange.getMessage().getBody(String.class)).isNotNull();
        // The LLM should have discovered the getOrderHistory tool and used it
        assertThat(exchange.getMessage().getBody(String.class)).containsIgnoringCase("123");
    }
}
