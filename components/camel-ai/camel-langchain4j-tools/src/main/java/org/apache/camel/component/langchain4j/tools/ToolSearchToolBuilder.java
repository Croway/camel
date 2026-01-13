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

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;

/**
 * Factory class to create the built-in "search_available_tools" ToolSpecification that is exposed to the LLM when
 * searchable tools (exposed=false) are present.
 */
public final class ToolSearchToolBuilder {

    /**
     * The name of the tool search tool.
     */
    public static final String TOOL_SEARCH_TOOL_NAME = "search_available_tools";

    private ToolSearchToolBuilder() {
        // Utility class
    }

    /**
     * Build the tool search tool specification.
     *
     * @return the ToolSpecification for the search_available_tools meta-tool
     */
    public static ToolSpecification buildToolSearchTool() {
        return ToolSpecification.builder()
                .name(TOOL_SEARCH_TOOL_NAME)
                .description(
                        "Search for available tools based on a natural language query. " +
                             "Use this when you need to find a tool to accomplish a specific task. " +
                             "Returns a list of tools that match your query, which you can then use.")
                .parameters(JsonObjectSchema.builder()
                        .addProperty("query", JsonStringSchema.builder()
                                .description("A natural language description of the capability you're looking for. " +
                                             "For example: 'find user information', 'calculate financial data', " +
                                             "'query database records'")
                                .build())
                        .required("query")
                        .build())
                .build();
    }
}
