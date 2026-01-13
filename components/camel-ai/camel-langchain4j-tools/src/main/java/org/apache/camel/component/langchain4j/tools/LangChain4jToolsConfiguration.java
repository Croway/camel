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

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.spi.Configurer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;

@Configurer
@UriParams
public class LangChain4jToolsConfiguration implements Cloneable {

    @UriParam(label = "advanced")
    @Metadata(autowired = true)
    private ChatModel chatModel;

    @UriParam(label = "advanced",
              description = "Embedding Model for tool search. Required when using tools with exposed=false.")
    @Metadata(autowired = true)
    private EmbeddingModel embeddingModel;

    @UriParam(label = "advanced", defaultValue = "5",
              description = "Maximum number of tools to return from tool search")
    private int toolSearchMaxResults = 5;

    @UriParam(label = "advanced", defaultValue = "0.5",
              description = "Minimum similarity score for tool search results (0.0 to 1.0)")
    private double toolSearchMinScore = 0.5;

    public LangChain4jToolsConfiguration() {
    }

    /**
     * Chat Model of type dev.langchain4j.model.chat.ChatModel
     *
     * @return
     */
    public ChatModel getChatModel() {
        return chatModel;
    }

    public void setChatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Embedding Model of type dev.langchain4j.model.embedding.EmbeddingModel. Required when using tools with
     * exposed=false for semantic tool search.
     *
     * @return the embedding model
     */
    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * Maximum number of tools to return from tool search.
     *
     * @return the maximum results
     */
    public int getToolSearchMaxResults() {
        return toolSearchMaxResults;
    }

    public void setToolSearchMaxResults(int toolSearchMaxResults) {
        this.toolSearchMaxResults = toolSearchMaxResults;
    }

    /**
     * Minimum similarity score for tool search results (0.0 to 1.0).
     *
     * @return the minimum score
     */
    public double getToolSearchMinScore() {
        return toolSearchMinScore;
    }

    public void setToolSearchMinScore(double toolSearchMinScore) {
        this.toolSearchMinScore = toolSearchMinScore;
    }

    public LangChain4jToolsConfiguration copy() {
        try {
            return (LangChain4jToolsConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeCamelException(e);
        }
    }
}
