package org.epam.learn.e2e;

import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

public final class SharedStack {

    public static final ComposeContainer STACK;

    static {
        STACK = new ComposeContainer(composeFile())
                .withBuild(false)
                .withExposedService("gateway", 8080,
                        Wait.forListeningPort()
                                .withStartupTimeout(Duration.ofMinutes(10)));
        STACK.start();
        Runtime.getRuntime().addShutdownHook(new Thread(STACK::stop));
    }

    public static String gatewayBaseUri() {
        return "http://" + STACK.getServiceHost("gateway", 8080)
                + ":" + STACK.getServicePort("gateway", 8080);
    }

    private static File composeFile() {
        // Works both when Maven runs from e2e-tests/ and when IDE runs from project root
        for (String candidate : new String[]{"compose-e2e.yaml", "../compose-e2e.yaml"}) {
            File f = new File(candidate).getAbsoluteFile();
            if (f.exists()) {
                return f;
            }
        }
        throw new IllegalStateException(
                "compose-e2e.yaml not found relative to: " + new File(".").getAbsolutePath());
    }

    private SharedStack() {}
}
