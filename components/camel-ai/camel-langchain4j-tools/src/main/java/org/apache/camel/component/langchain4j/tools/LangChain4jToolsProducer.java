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
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.Response;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.TypeConverter;
import org.apache.camel.component.langchain4j.tools.spec.CamelToolExecutorCache;
import org.apache.camel.component.langchain4j.tools.spec.CamelToolSpecification;
import org.apache.camel.component.langchain4j.tools.spec.SearchableToolRegistry;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LangChain4jToolsProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(LangChain4jToolsProducer.class);

    private final LangChain4jToolsEndpoint endpoint;

    private ChatModel chatModel;
    private EmbeddingModel embeddingModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LangChain4jToolsProducer(LangChain4jToolsEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        processMultipleMessages(exchange);
    }

    @SuppressWarnings("unchecked")
    private void processMultipleMessages(Exchange exchange) throws InvalidPayloadException {
        List<ChatMessage> messages = exchange.getIn().getMandatoryBody(List.class);

        final String response = toolsChat(messages, exchange);
        populateResponse(response, exchange);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        this.chatModel = this.endpoint.getConfiguration().getChatModel();
        this.embeddingModel = this.endpoint.getConfiguration().getEmbeddingModel();
        ObjectHelper.notNull(chatModel, "chatModel");
        // embeddingModel is optional, only required for tool search feature
    }

    private void populateResponse(String response, Exchange exchange) {
        exchange.getMessage().setBody(response);
    }

    private boolean isMatch(String[] tags, Map.Entry<String, Set<CamelToolSpecification>> entry) {
        for (String tag : tags) {
            if (entry.getKey().equals(tag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Send a ChatMessage
     *
     * @param  chatMessages
     * @return
     */
    private String toolsChat(List<ChatMessage> chatMessages, Exchange exchange) {
        final CamelToolExecutorCache toolCache = CamelToolExecutorCache.getInstance();

        final ToolPair toolPair = computeCandidates(toolCache, exchange);
        if (toolPair == null) {
            return null;
        }

        // First talk to the model to get the tools to be called
        int i = 0;
        do {
            LOG.debug("Starting iteration {}", i);
            final Response<AiMessage> response = chatWithLLM(chatMessages, toolPair, exchange);
            if (isDoneExecuting(response)) {
                return extractAiResponse(response);
            }

            // Only invoke the tools ... the response will be computed on the next loop
            invokeTools(chatMessages, exchange, response, toolPair);
            LOG.debug("Finished iteration {}", i);
            i++;
        } while (true);
    }

    private boolean isDoneExecuting(Response<AiMessage> response) {
        if (!response.content().hasToolExecutionRequests()) {
            LOG.info("Finished executing tools because of there are no more execution requests");
            return true;
        }

        if (response.finishReason() != null) {
            LOG.info("Finished executing tools because of {}", response.finishReason());

            if (response.finishReason() == FinishReason.STOP) {
                return true;
            }
        }

        return false;
    }

    private void invokeTools(
            List<ChatMessage> chatMessages, Exchange exchange, Response<AiMessage> response, ToolPair toolPair) {
        int i = 0;
        List<ToolExecutionRequest> toolExecutionRequests = response.content().toolExecutionRequests();
        for (ToolExecutionRequest toolExecutionRequest : toolExecutionRequests) {
            String toolName = toolExecutionRequest.name();
            LOG.info("Invoking tool {} ({}) of {}", i, toolName, toolExecutionRequests.size());

            // Check if this is the tool-search-tool (special handling)
            if (ToolSearchToolBuilder.TOOL_SEARCH_TOOL_NAME.equals(toolName)) {
                String result = handleToolSearch(toolExecutionRequest, toolPair);
                chatMessages.add(new ToolExecutionResultMessage(
                        toolExecutionRequest.id(),
                        toolExecutionRequest.name(),
                        result));
                i++;
                continue;
            }

            // Find the tool in callable tools
            final CamelToolSpecification camelToolSpecification = toolPair.callableTools().stream()
                    .filter(c -> c.getToolSpecification().name().equals(toolName))
                    .findFirst()
                    .orElse(null);

            if (camelToolSpecification == null) {
                LOG.warn("Tool {} not found in callable tools", toolName);
                chatMessages.add(new ToolExecutionResultMessage(
                        toolExecutionRequest.id(),
                        toolExecutionRequest.name(),
                        "Error: Tool '" + toolName + "' not found. Use search_available_tools to discover available tools."));
                i++;
                continue;
            }

            try {
                TypeConverter typeConverter = endpoint.getCamelContext().getTypeConverter();

                // Map Json to Header
                JsonNode jsonNode = objectMapper.readValue(toolExecutionRequest.arguments(), JsonNode.class);
                jsonNode.fieldNames()
                        .forEachRemaining(name -> {
                            final JsonNode value = jsonNode.get(name);
                            Object headerValue;

                            // Try to get values for the known tool parameter types
                            if (value instanceof TextNode) {
                                headerValue = typeConverter.convertTo(String.class, value);
                            } else if (value instanceof IntNode) {
                                headerValue = typeConverter.convertTo(Integer.class, value);
                            } else if (value instanceof LongNode) {
                                headerValue = typeConverter.convertTo(Long.class, value);
                            } else if (value instanceof DoubleNode) {
                                headerValue = typeConverter.convertTo(Double.class, value);
                            } else if (value instanceof BooleanNode) {
                                headerValue = typeConverter.convertTo(Boolean.class, value);
                            } else {
                                // Fallback to JsonNode to enable the value to be extracted elsewhere
                                headerValue = value;
                            }

                            exchange.getMessage().setHeader(name, headerValue);
                        });

                // Execute the consumer route

                camelToolSpecification.getConsumer().getProcessor().process(exchange);
                i++;
            } catch (Exception e) {
                // How to handle this exception?
                exchange.setException(e);
            }

            chatMessages.add(new ToolExecutionResultMessage(
                    toolExecutionRequest.id(),
                    toolExecutionRequest.name(),
                    exchange.getIn().getBody(String.class)));
        }
    }

    /**
     * Handle the tool search meta-tool invocation. Performs semantic search for matching tools and adds them to the
     * toolPair for subsequent invocation.
     *
     * @param  request  the tool execution request
     * @param  toolPair the current tool pair to add discovered tools to
     * @return          the result message to return to the LLM
     */
    private String handleToolSearch(ToolExecutionRequest request, ToolPair toolPair) {
        try {
            JsonNode jsonNode = objectMapper.readValue(request.arguments(), JsonNode.class);
            String query = jsonNode.get("query").asText();

            LOG.info("Executing tool search with query: {}", query);

            String[] tags = TagsHelper.splitTags(endpoint.getTags());
            int maxResults = endpoint.getConfiguration().getToolSearchMaxResults();
            double minScore = endpoint.getConfiguration().getToolSearchMinScore();

            List<CamelToolSpecification> matchedTools = SearchableToolRegistry.getInstance()
                    .searchTools(query, embeddingModel, tags, maxResults, minScore);

            if (matchedTools.isEmpty()) {
                return "No matching tools found for query: '" + query + "'. Try a different search query.";
            }

            // Add matched tools to the toolPair for this conversation
            for (CamelToolSpecification spec : matchedTools) {
                if (!toolPair.callableTools().contains(spec)) {
                    toolPair.callableTools().add(spec);
                    toolPair.toolSpecifications().add(spec.getToolSpecification());
                    LOG.debug("Added discovered tool: {}", spec.getToolSpecification().name());
                }
            }

            // Format tool descriptions for LLM
            StringBuilder sb = new StringBuilder();
            sb.append("Found ").append(matchedTools.size()).append(" matching tool(s):\n\n");
            for (CamelToolSpecification spec : matchedTools) {
                ToolSpecification ts = spec.getToolSpecification();
                sb.append("**").append(ts.name()).append("**: ").append(ts.description()).append("\n");
                JsonObjectSchema params = ts.parameters();
                if (params != null && params.properties() != null && !params.properties().isEmpty()) {
                    sb.append("  Parameters: ");
                    sb.append(String.join(", ", params.properties().keySet()));
                    sb.append("\n");
                }
                sb.append("\n");
            }
            sb.append("You can now use these tools to complete your task.");

            return sb.toString();
        } catch (Exception e) {
            LOG.error("Error executing tool search", e);
            return "Error searching for tools: " + e.getMessage();
        }
    }

    /**
     * This talks with the LLM to, passing the list of tools, and expects a response listing one ore more tools to be
     * called
     *
     * @param  chatMessages the input chat messages
     * @param  toolPair     the toolPair containing the available tools to be called
     * @return              the response provided by the model
     */
    private Response<AiMessage> chatWithLLM(List<ChatMessage> chatMessages, ToolPair toolPair, Exchange exchange) {

        ChatRequest.Builder requestBuilder = ChatRequest.builder()
                .messages(chatMessages);

        // Add tools if available
        if (toolPair != null && toolPair.toolSpecifications() != null) {
            requestBuilder.toolSpecifications(toolPair.toolSpecifications());
        }

        // build request
        ChatRequest chatRequest = requestBuilder.build();

        // generate response
        ChatResponse chatResponse = this.chatModel.chat(chatRequest);

        // Convert ChatResponse to Response<AiMessage> for compatibility
        AiMessage aiMessage = chatResponse.aiMessage();
        Response<AiMessage> response = Response.from(aiMessage);

        if (!response.content().hasToolExecutionRequests()) {
            exchange.getMessage().setHeader(LangChain4jTools.NO_TOOLS_CALLED_HEADER, Boolean.TRUE);
            return response;
        }

        chatMessages.add(response.content());
        return response;
    }

    /**
     * This method traverses all tag sets to search for the tools that match the tags for the current endpoint.
     *
     * @param  toolCache the global cache of tools
     * @return           It returns a ToolPair containing both the specification, and the {@link CamelToolSpecification}
     *                   that can be used to call the endpoints.
     */
    private ToolPair computeCandidates(CamelToolExecutorCache toolCache, Exchange exchange) {
        final List<ToolSpecification> toolSpecifications = new ArrayList<>();
        final List<CamelToolSpecification> callableTools = new ArrayList<>();

        final Map<String, Set<CamelToolSpecification>> tools = toolCache.getTools();
        String[] tags = TagsHelper.splitTags(endpoint.getTags());
        for (var entry : tools.entrySet()) {
            if (isMatch(tags, entry)) {
                final List<CamelToolSpecification> callablesForTag = entry.getValue().stream()
                        .toList();

                callableTools.addAll(callablesForTag);

                final List<ToolSpecification> toolsForTag = entry.getValue().stream()
                        .map(CamelToolSpecification::getToolSpecification)
                        .toList();

                toolSpecifications.addAll(toolsForTag);
            }
        }

        // Check if there are searchable tools for these tags and add the tool-search-tool
        final SearchableToolRegistry searchableRegistry = SearchableToolRegistry.getInstance();
        if (searchableRegistry.hasSearchableTools(tags) && embeddingModel != null) {
            LOG.debug("Adding search_available_tools meta-tool for searchable tools");
            toolSpecifications.add(ToolSearchToolBuilder.buildToolSearchTool());
            // Note: tool-search-tool is handled specially in invokeTools, not added to callableTools
        }

        if (toolSpecifications.isEmpty()) {
            exchange.getMessage().setHeader(LangChain4jTools.NO_TOOLS_CALLED_HEADER, Boolean.TRUE);
            return null;
        }

        return new ToolPair(toolSpecifications, callableTools);
    }

    /**
     * The pair of tools specifications and the Camel tools (i.e.: routes) that can be called for that set. This class
     * is mutable to allow dynamically discovered tools to be added during tool search.
     */
    private static class ToolPair {
        private final List<ToolSpecification> toolSpecifications;
        private final List<CamelToolSpecification> callableTools;

        ToolPair(List<ToolSpecification> toolSpecifications, List<CamelToolSpecification> callableTools) {
            this.toolSpecifications = new ArrayList<>(toolSpecifications);
            this.callableTools = new ArrayList<>(callableTools);
        }

        List<ToolSpecification> toolSpecifications() {
            return toolSpecifications;
        }

        List<CamelToolSpecification> callableTools() {
            return callableTools;
        }
    }

    private String extractAiResponse(Response<AiMessage> response) {
        AiMessage message = response.content();
        return message == null ? null : message.text();
    }

}
