package org.apache.camel.junit;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

public class FipsTestExecutionListener implements TestExecutionListener {

    private static final Logger LOG = LoggerFactory.getLogger(FipsTestExecutionListener.class);

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        LOG.info("Executing test {}", testIdentifier.getDisplayName());
        Arrays.stream(Security.getProviders()).forEach(sp -> LOG.info("security provider {}", sp.getInfo()));
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        LOG.info("Finished executing test {}", testIdentifier.getDisplayName());
        Arrays.stream(Security.getProviders()).forEach(sp -> LOG.info("security provider {}", sp.getInfo()));
    }
}
