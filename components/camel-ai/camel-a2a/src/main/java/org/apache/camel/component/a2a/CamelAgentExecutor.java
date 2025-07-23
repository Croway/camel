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

import io.a2a.A2A;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.DataPart;
import io.a2a.spec.FilePart;
import io.a2a.spec.FileWithBytes;
import io.a2a.spec.FileWithUri;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TextPart;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.support.BridgeExceptionHandlerToErrorHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CamelAgentExecutor implements AgentExecutor {

    private A2AConsumer a2AConsumer;
    private TaskStore taskStore;
    private boolean task;

    public CamelAgentExecutor(A2AConsumer a2AConsumer, TaskStore taskStore, boolean task) {
        this.a2AConsumer = a2AConsumer;
        this.taskStore = taskStore;
        this.task = task;
    }

    @Override
    public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        Exchange exchange = a2AConsumer.createExchange(true);

        exchange.getMessage().setHeader("CamelA2AOriginalMessage", context.getMessage());
        exchange.getMessage().setHeader("CamelA2ARequestContext", context);
        exchange.getMessage().setHeader("CamelA2AEventQueue", eventQueue);
        exchange.getMessage().setHeader("CamelA2ATaskId", context.getTaskId());

        try {
            if (!task) {
                setExchangeMessage(context, exchange);
                a2AConsumer.getProcessor().process(exchange);

                eventQueue.enqueueEvent(A2A.toAgentMessage(exchange.getMessage().getBody(String.class)));
            } else {
                Task storedTask = taskStore.get(context.getTaskId());
                if (storedTask == null) {
                    Task newTask = new Task(
                            context.getTaskId(),
                            context.getContextId(),
                            new TaskStatus(
                                    TaskState.SUBMITTED,
                                    context.getMessage(),
                                    LocalDateTime.now()
                            ),
                            null,
                            List.of(context.getMessage()),
                            context.getMessage().getMetadata());

                    taskStore.save(newTask);

                    setExchangeMessage(context, exchange);
                    exchange.getMessage().setHeader("CamelA2ATask", newTask);

                    a2AConsumer.getAsyncProcessor().process(exchange, doneSync -> {
                        if (doneSync) {
                            List<Message> history = new ArrayList<>();
                            history.addAll(newTask.getHistory());
                            history.add(A2A.toAgentMessage(exchange.getMessage().getBody(String.class)));

                            Task updatedTask = new Task(
                                    newTask.getId(),
                                    newTask.getContextId(),
                                    new TaskStatus(
                                            TaskState.COMPLETED
                                    ),
                                    newTask.getArtifacts(),
                                    history,
                                    newTask.getMetadata()
                            );

                            taskStore.save(updatedTask);
                        } else {
                            // ??
                            Task failedTask = new Task(
                                    newTask.getId(),
                                    newTask.getContextId(),
                                    new TaskStatus(
                                            TaskState.FAILED
                                    ),
                                    null,
                                    null,
                                    null
                            );

                            taskStore.save(failedTask);
                        }
                    });

                    eventQueue.enqueueEvent(A2A.toAgentMessage(newTask.getId()));
                } else {
                    eventQueue.enqueueEvent(storedTask);
                }
            }
        } catch (Exception e) {
            BridgeExceptionHandlerToErrorHandler bridge = new BridgeExceptionHandlerToErrorHandler(a2AConsumer);
            bridge.handleException(e);
            eventQueue.enqueueEvent(A2A.toAgentMessage(e.getMessage()));
        }
    }

    private static void setExchangeMessage(RequestContext context, Exchange exchange) {
        List<String> messages = new ArrayList<>();
        for (Part part : context.getMessage().getParts()) {
            if (part instanceof TextPart textPart) {
                messages.add(textPart.getText());
            } else if (part instanceof FilePart filePart) {
                if (filePart.getFile() instanceof FileWithBytes fileWithBytes) {
                    // TODO
                } else if (filePart.getFile() instanceof FileWithUri fileWithUri) {
                    // TODO
                }
            } else if(part instanceof  DataPart dataPart) {
                // TODO
            }
        }

        exchange.getMessage().setBody(messages.size() == 1 ? messages.get(0) : messages);
    }

    @Override
    public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        // TODO
    }
}
