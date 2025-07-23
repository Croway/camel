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
package org.apache.camel.component.a2a;

import java.util.List;
import java.util.Map;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentInterface;
import io.a2a.spec.AgentProvider;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.SecurityScheme;
import org.apache.camel.spi.UriParams;
import org.apache.camel.spi.UriPath;

@UriParams
public class A2AConfiguration {

    @UriPath
    private String name;

    private AgentCard agentCard;

    private String description;
    private String url;
    private AgentProvider provider;
    private String version;
    private String documentationUrl;
    private AgentCapabilities capabilities;
    private String defaultInputModes;
    private String defaultOutputModes;
    private List<AgentSkill> skills;
    private boolean supportsAuthenticatedExtendedCard;
    private Map<String, SecurityScheme> securitySchemes;
    private List<Map<String, List<String>>> security;
    private String iconUrl;
    private List<AgentInterface> additionalInterfaces;
    private String preferredTransport;
    private String protocolVersion;
    private int port;
    // Configure the message response as a task if true, or a standard message
    private boolean task;

    public String getName() {
        return name;
    }

    /**
     * The name of the agent
     */
    public void setName(String name) {
        this.name = name;
    }

    public AgentCard getAgentCard() {
        return agentCard;
    }

    /**
     * The agent card to use
     */
    public void setAgentCard(AgentCard agentCard) {
        this.agentCard = agentCard;
    }

    public String getDescription() {
        return description;
    }

    /**
     * The description of the agent
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    /**
     * The URL of the agent
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public AgentProvider getProvider() {
        return provider;
    }

    /**
     * The provider of the agent
     */
    public void setProvider(AgentProvider provider) {
        this.provider = provider;
    }

    public String getVersion() {
        return version;
    }

    /**
     * The version of the agent
     */
    public void setVersion(String version) {
        this.version = version;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    /**
     * The documentation URL of the agent
     */
    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public AgentCapabilities getCapabilities() {
        return capabilities;
    }

    /**
     * The capabilities of the agent
     */
    public void setCapabilities(AgentCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public String getDefaultInputModes() {
        return defaultInputModes;
    }

    /**
     * The default input modes of the agent
     */
    public void setDefaultInputModes(String defaultInputModes) {
        this.defaultInputModes = defaultInputModes;
    }

    public String getDefaultOutputModes() {
        return defaultOutputModes;
    }

    /**
     * The default output modes of the agent
     */
    public void setDefaultOutputModes(String defaultOutputModes) {
        this.defaultOutputModes = defaultOutputModes;
    }

    public List<AgentSkill> getSkills() {
        return skills;
    }

    /**
     * The skills of the agent
     */
    public void setSkills(List<AgentSkill> skills) {
        this.skills = skills;
    }

    public boolean isSupportsAuthenticatedExtendedCard() {
        return supportsAuthenticatedExtendedCard;
    }

    /**
     * Whether the agent supports authenticated extended card
     */
    public void setSupportsAuthenticatedExtendedCard(boolean supportsAuthenticatedExtendedCard) {
        this.supportsAuthenticatedExtendedCard = supportsAuthenticatedExtendedCard;
    }

    public Map<String, SecurityScheme> getSecuritySchemes() {
        return securitySchemes;
    }

    /**
     * The security schemes of the agent
     */
    public void setSecuritySchemes(Map<String, SecurityScheme> securitySchemes) {
        this.securitySchemes = securitySchemes;
    }

    public List<Map<String, List<String>>> getSecurity() {
        return security;
    }

    /**
     * The security of the agent
     */
    public void setSecurity(List<Map<String, List<String>>> security) {
        this.security = security;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * The icon URL of the agent
     */
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<AgentInterface> getAdditionalInterfaces() {
        return additionalInterfaces;
    }

    /**
     * The additional interfaces of the agent
     */
    public void setAdditionalInterfaces(List<AgentInterface> additionalInterfaces) {
        this.additionalInterfaces = additionalInterfaces;
    }

    public String getPreferredTransport() {
        return preferredTransport;
    }

    /**
     * The preferred transport of the agent
     */
    public void setPreferredTransport(String preferredTransport) {
        this.preferredTransport = preferredTransport;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * The protocol version of the agent
     */
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isTask() {
        return task;
    }

    public void setTask(boolean task) {
        this.task = task;
    }
}
