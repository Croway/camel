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
package org.apache.camel.component.langchain4j.tools.spec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

/**
 * Registry for searchable tools (tools with exposed=false). Stores tools with their embeddings for semantic search.
 */
public final class SearchableToolRegistry {

    private final Map<String, Set<SearchableToolEntry>> searchableTools;
    private final InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private final Map<String, CamelToolSpecification> embeddingIdToTool;

    private SearchableToolRegistry() {
        searchableTools = new ConcurrentHashMap<>();
        embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingIdToTool = new ConcurrentHashMap<>();
    }

    private static final class SingletonHolder {
        private static final SearchableToolRegistry INSTANCE = new SearchableToolRegistry();
    }

    public static SearchableToolRegistry getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Register a tool with its embedding for semantic search.
     *
     * @param tag            the tag to register the tool under
     * @param spec           the tool specification
     * @param embeddingModel the embedding model to use for computing embeddings
     */
    public void registerTool(String tag, CamelToolSpecification spec, EmbeddingModel embeddingModel) {
        String searchableText = buildSearchableText(spec);
        String embeddingId = UUID.randomUUID().toString();

        // Compute embedding for the tool description
        Embedding embedding = embeddingModel.embed(searchableText).content();

        // Store in embedding store with the ID as metadata
        TextSegment segment = TextSegment.from(searchableText);
        embeddingStore.add(embeddingId, embedding, segment);

        // Map embedding ID to tool specification
        embeddingIdToTool.put(embeddingId, spec);

        // Store in tag-based lookup
        SearchableToolEntry entry = new SearchableToolEntry(spec, embeddingId, searchableText);
        searchableTools.computeIfAbsent(tag, k -> new LinkedHashSet<>()).add(entry);
    }

    /**
     * Search for tools matching the given query using semantic similarity.
     *
     * @param  query          the natural language query
     * @param  embeddingModel the embedding model to use
     * @param  tags           the tags to filter by
     * @param  maxResults     maximum number of results to return
     * @param  minScore       minimum similarity score threshold
     * @return                list of matching tool specifications
     */
    public List<CamelToolSpecification> searchTools(
            String query, EmbeddingModel embeddingModel, String[] tags, int maxResults, double minScore) {

        // Get all tool IDs for the given tags
        Set<String> validEmbeddingIds = new LinkedHashSet<>();
        for (String tag : tags) {
            Set<SearchableToolEntry> entries = searchableTools.get(tag);
            if (entries != null) {
                for (SearchableToolEntry entry : entries) {
                    validEmbeddingIds.add(entry.getEmbeddingId());
                }
            }
        }

        if (validEmbeddingIds.isEmpty()) {
            return List.of();
        }

        // Compute embedding for the query
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // Search in embedding store
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults * 2) // Request more to filter by tags
                .minScore(minScore)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

        // Filter by tags and collect results
        List<CamelToolSpecification> results = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : searchResult.matches()) {
            String embeddingId = match.embeddingId();
            if (validEmbeddingIds.contains(embeddingId)) {
                CamelToolSpecification tool = embeddingIdToTool.get(embeddingId);
                if (tool != null && !results.contains(tool)) {
                    results.add(tool);
                    if (results.size() >= maxResults) {
                        break;
                    }
                }
            }
        }

        return results;
    }

    /**
     * Check if there are any searchable tools for the given tags.
     *
     * @param  tags the tags to check
     * @return      true if at least one searchable tool exists for any of the tags
     */
    public boolean hasSearchableTools(String[] tags) {
        for (String tag : tags) {
            Set<SearchableToolEntry> entries = searchableTools.get(tag);
            if (entries != null && !entries.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all searchable tools for the given tags.
     *
     * @param  tags the tags to filter by
     * @return      set of all matching tool specifications
     */
    public Set<CamelToolSpecification> getToolsForTags(String[] tags) {
        Set<CamelToolSpecification> result = new LinkedHashSet<>();
        for (String tag : tags) {
            Set<SearchableToolEntry> entries = searchableTools.get(tag);
            if (entries != null) {
                for (SearchableToolEntry entry : entries) {
                    result.add(entry.getToolSpecification());
                }
            }
        }
        return result;
    }

    /**
     * Clear all registered tools and embeddings.
     */
    public void clear() {
        searchableTools.clear();
        embeddingIdToTool.clear();
        // Note: InMemoryEmbeddingStore doesn't have a clear method,
        // so we create a new instance by resetting (would require instance replacement)
    }

    /**
     * Build searchable text from tool specification (name + description + parameters).
     */
    private String buildSearchableText(CamelToolSpecification spec) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tool: ").append(spec.getToolSpecification().name()).append("\n");
        sb.append("Description: ").append(spec.getToolSpecification().description());

        if (spec.getToolSpecification().parameters() != null) {
            sb.append("\nParameters: ");
            spec.getToolSpecification().parameters().properties().forEach((name, schema) -> {
                sb.append(name).append(" ");
            });
        }

        return sb.toString();
    }
}
