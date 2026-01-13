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
package org.apache.camel.component.langchain4j.tools;

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
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.tools.spec.SearchableToolRegistry;
import org.apache.camel.test.infra.openai.mock.OpenAIMock;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * End-to-end test for the Tool Search feature. Tests that:
 * <ul>
 * <li>Tools with exposed=false are not directly visible to the LLM</li>
 * <li>The search_available_tools meta-tool is provided when searchable tools exist</li>
 * <li>The LLM can discover and use searchable tools</li>
 * </ul>
 */
public class LangChain4jToolSearchTest extends CamelTestSupport {

    protected final String nameFromDB = "pippo";
    protected ChatModel chatModel;
    protected EmbeddingModel embeddingModel;

    @RegisterExtension
    static OpenAIMock openAIMock = new OpenAIMock().builder()
            // First, the LLM will search for tools
            .when("What is the name of the user 1?\n")
            .invokeTool("search_available_tools")
            .withParam("query", "find user information by ID")
            .andThenInvokeTool("getUserById")
            .withParam("userId", 1)
            .build();

    @Override
    protected void setupResources() throws Exception {
        super.setupResources();

        chatModel = ToolsHelper.createModel(openAIMock.getBaseUrl());
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
                        .to("langchain4j-tools:test1?tags=user")
                        .log("response is: ${body}");

                // This tool is NOT directly exposed (exposed=false), so it requires search to discover
                from("langchain4j-tools:searchableUserTool?tags=user&name=getUserById"
                     + "&description=Retrieve user information from database by their unique identifier"
                     + "&exposed=false&parameter.userId=integer")
                        .setBody(simple("{\"name\": \"pippo\"}"));
            }
        };
    }

    @Test
    public void testToolSearchAndInvocation() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(
                """
                        You provide the requested information using the functions you have available.
                        If you don't see a direct function, use search_available_tools to discover available functions.
                        You can invoke the functions to obtain the information you need to complete the answer.
                        """));
        messages.add(new UserMessage("""
                What is the name of the user 1?
                """));

        Exchange exchange = fluentTemplate.to("direct:test").withBody(messages).request(Exchange.class);

        Assertions.assertThat(exchange).isNotNull();
        Message message = exchange.getMessage();
        Assertions.assertThat(message.getBody(String.class)).containsIgnoringCase(nameFromDB);
    }
}
