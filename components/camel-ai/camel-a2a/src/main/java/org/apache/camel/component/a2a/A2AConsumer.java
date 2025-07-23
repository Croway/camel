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

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.a2a.server.events.InMemoryQueueManager;
import io.a2a.server.requesthandlers.DefaultRequestHandler;
import io.a2a.server.requesthandlers.JSONRPCHandler;
import io.a2a.server.tasks.InMemoryTaskStore;
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.AgentCard;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.DeleteTaskPushNotificationConfigRequest;
import io.a2a.spec.GetTaskPushNotificationConfigRequest;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.IdJsonMappingException;
import io.a2a.spec.InternalError;
import io.a2a.spec.InvalidParamsError;
import io.a2a.spec.InvalidParamsJsonMappingException;
import io.a2a.spec.InvalidRequestError;
import io.a2a.spec.JSONParseError;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.JSONRPCErrorResponse;
import io.a2a.spec.JSONRPCRequest;
import io.a2a.spec.JSONRPCResponse;
import io.a2a.spec.ListTaskPushNotificationConfigRequest;
import io.a2a.spec.MethodNotFoundError;
import io.a2a.spec.MethodNotFoundJsonMappingException;
import io.a2a.spec.NonStreamingJSONRPCRequest;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SetTaskPushNotificationConfigRequest;
import io.a2a.spec.UnsupportedOperationError;
import io.a2a.util.Utils;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import org.apache.camel.Processor;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpRouter;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpServer;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpServerConfiguration;
import org.apache.camel.support.DefaultConsumer;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

public class A2AConsumer extends DefaultConsumer {

    private VertxPlatformHttpServer vertxPlatformHttpServer;
    private JSONRPCHandler jsonRpcHandler;
    private final String agentName;

    public A2AConsumer(A2AEndpoint endpoint, Processor processor, String agentName) {
        super(endpoint, processor);
        this.agentName = agentName;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        A2AConfiguration configuration = getEndpoint().getConfiguration();

        VertxPlatformHttpServerConfiguration conf = new VertxPlatformHttpServerConfiguration();
        conf.setBindPort(configuration.getPort());

        vertxPlatformHttpServer = new VertxPlatformHttpServer(conf);
        getEndpoint().getCamelContext().addService(vertxPlatformHttpServer);
        vertxPlatformHttpServer.init();
        vertxPlatformHttpServer.start();

        AgentCard agentCard = configuration.getAgentCard();
        if (agentCard == null) {
            agentCard = new AgentCard(
                    agentName,
                    configuration.getDescription(),
                    configuration.getUrl(),
                    configuration.getProvider(),
                    configuration.getVersion(),
                    configuration.getDocumentationUrl(),
                    configuration.getCapabilities(),
                    Arrays.stream(configuration.getDefaultInputModes().split(",")).toList(),
                    Arrays.stream(configuration.getDefaultOutputModes().split(",")).toList(),
                    configuration.getSkills(),
                    configuration.isSupportsAuthenticatedExtendedCard(),
                    configuration.getSecuritySchemes(),
                    configuration.getSecurity(),
                    configuration.getIconUrl(),
                    configuration.getAdditionalInterfaces(),
                    configuration.getPreferredTransport(),
                    configuration.getProtocolVersion());
        }
        final AgentCard finalAgentCard = agentCard;

        TaskStore taskStore = new InMemoryTaskStore();
        jsonRpcHandler = new JSONRPCHandler(
                agentCard,
                new DefaultRequestHandler(
                        new CamelAgentExecutor(this, taskStore, configuration.isTask()),
                        taskStore,
                        new InMemoryQueueManager(),
                        null, null,
                        getEndpoint().getCamelContext().getExecutorServiceManager().newSingleThreadExecutor(this,
                                "a2a-" + configuration.getPort())));

        ObjectMapper mapper = new ObjectMapper();

        VertxPlatformHttpRouter router = getEndpoint().getCamelContext().getRegistry().lookupByNameAndType(
                VertxPlatformHttpRouter.PLATFORM_HTTP_ROUTER_NAME + "-" + configuration.getPort(),
                VertxPlatformHttpRouter.class);

        router.post()
                .path("/")
                .method(HttpMethod.POST)
                .consumes("application/json")
                .blockingHandler(routingContext -> routingContext.request().bodyHandler(bodyHandler -> {
                    final String body = bodyHandler.toString();

                    JSONRPCResponse<?> nonStreamingResponse = null;
                    JSONRPCErrorResponse error = null;

                    try {
                        NonStreamingJSONRPCRequest<?> request = Utils.OBJECT_MAPPER.readValue(body,
                                NonStreamingJSONRPCRequest.class);
                        nonStreamingResponse = processNonStreamingRequest(request);
                    } catch (JsonProcessingException e) {
                        error = handleError(e);
                    } catch (Throwable t) {
                        error = new JSONRPCErrorResponse(new InternalError(t.getMessage()));
                    } finally {
                        if (error != null) {
                            routingContext.response()
                                    .setStatusCode(200)
                                    .putHeader(CONTENT_TYPE, "application/json")
                                    .end(Json.encodeToBuffer(error));
                        } else {
                            routingContext.response()
                                    .setStatusCode(200)
                                    .putHeader(CONTENT_TYPE, "application/json")
                                    .end(Json.encodeToBuffer(nonStreamingResponse));
                        }
                    }
                }));

        router.get("/.well-known/agent.json")
                .produces("application/json")
                .blockingHandler(routingContext -> {
                    try {
                        routingContext
                                .response()
                                .setStatusCode(200)
                                .end(mapper.writeValueAsString(finalAgentCard));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

    }

    private JSONRPCResponse<?> processNonStreamingRequest(NonStreamingJSONRPCRequest<?> request) {
        if (request instanceof GetTaskRequest) {
            return jsonRpcHandler.onGetTask((GetTaskRequest) request);
        } else if (request instanceof CancelTaskRequest) {
            return jsonRpcHandler.onCancelTask((CancelTaskRequest) request);
        } else if (request instanceof SetTaskPushNotificationConfigRequest) {
            return jsonRpcHandler.setPushNotificationConfig((SetTaskPushNotificationConfigRequest) request);
        } else if (request instanceof GetTaskPushNotificationConfigRequest) {
            return jsonRpcHandler.getPushNotificationConfig((GetTaskPushNotificationConfigRequest) request);
        } else if (request instanceof SendMessageRequest) {
            return jsonRpcHandler.onMessageSend((SendMessageRequest) request);
        } else if (request instanceof ListTaskPushNotificationConfigRequest) {
            return jsonRpcHandler.listPushNotificationConfig((ListTaskPushNotificationConfigRequest) request);
        } else if (request instanceof DeleteTaskPushNotificationConfigRequest) {
            return jsonRpcHandler.deletePushNotificationConfig((DeleteTaskPushNotificationConfigRequest) request);
        } else {
            return generateErrorResponse(request, new UnsupportedOperationError());
        }
    }

    private JSONRPCErrorResponse handleError(JsonProcessingException exception) {
        Object id = null;
        JSONRPCError jsonRpcError;
        if (exception.getCause() instanceof JsonParseException) {
            jsonRpcError = new JSONParseError();
        } else if (exception instanceof JsonEOFException) {
            jsonRpcError = new JSONParseError(exception.getMessage());
        } else if (exception instanceof MethodNotFoundJsonMappingException err) {
            id = err.getId();
            jsonRpcError = new MethodNotFoundError();
        } else if (exception instanceof InvalidParamsJsonMappingException err) {
            id = err.getId();
            jsonRpcError = new InvalidParamsError();
        } else if (exception instanceof IdJsonMappingException err) {
            id = err.getId();
            jsonRpcError = new InvalidRequestError();
        } else {
            jsonRpcError = new InvalidRequestError();
        }
        return new JSONRPCErrorResponse(id, jsonRpcError);
    }

    private JSONRPCResponse<?> generateErrorResponse(JSONRPCRequest<?> request, JSONRPCError error) {
        return new JSONRPCErrorResponse(request.getId(), error);
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        if (vertxPlatformHttpServer != null) {
            vertxPlatformHttpServer.stop();
        }
    }

    @Override
    public A2AEndpoint getEndpoint() {
        return (A2AEndpoint) super.getEndpoint();
    }
}
