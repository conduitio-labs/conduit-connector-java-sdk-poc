package io.conduit.sdk;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import org.eclipse.microprofile.config.ConfigProvider;

public class Main implements QuarkusApplication {
    @Override
    public int run(String... args) throws Exception {
        String portStr = ConfigProvider
            .getConfig()
            .getConfigValue("quarkus.grpc.server.port")
            .getValue()
            .trim();

        int port = Integer.parseInt(portStr);
        System.out.printf("1|1|tcp|localhost:%d|grpc%n", port);

        Quarkus.waitForExit();

        return 0;
    }
}
