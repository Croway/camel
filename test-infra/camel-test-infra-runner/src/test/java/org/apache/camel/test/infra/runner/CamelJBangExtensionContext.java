package org.apache.camel.test.infra.runner;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

public class CamelJBangExtensionContext implements ExtensionContext {

    private final NamespacedHierarchicalStore<Namespace> valuesStore = new NamespacedHierarchicalStore<>(null);

    @Override
    public Optional<ExtensionContext> getParent() {
        return Optional.empty();
    }

    @Override
    public ExtensionContext getRoot() {
        return this;
    }

    @Override
    public String getUniqueId() {
        return "camel-jbang";
    }

    @Override
    public String getDisplayName() {
        return "Camel JBang Extension Context";
    }

    @Override
    public Set<String> getTags() {
        return Set.of();
    }

    @Override
    public Optional<AnnotatedElement> getElement() {
        return Optional.empty();
    }

    @Override
    public Optional<Class<?>> getTestClass() {
        return Optional.empty();
    }

    @Override
    public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
        return Optional.empty();
    }

    @Override
    public Optional<Object> getTestInstance() {
        return Optional.empty();
    }

    @Override
    public Optional<TestInstances> getTestInstances() {
        return Optional.empty();
    }

    @Override
    public Optional<Method> getTestMethod() {
        return Optional.empty();
    }

    @Override
    public Optional<Throwable> getExecutionException() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getConfigurationParameter(String key) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> getConfigurationParameter(String key, Function<String, T> transformer) {
        return Optional.empty();
    }

    @Override
    public void publishReportEntry(Map<String, String> map) {

    }

    @Override
    public Store getStore(Namespace namespace) {
        return new NamespaceAwareStore(valuesStore, namespace);
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return null;
    }

    @Override
    public ExecutableInvoker getExecutableInvoker() {
        return null;
    }
}
