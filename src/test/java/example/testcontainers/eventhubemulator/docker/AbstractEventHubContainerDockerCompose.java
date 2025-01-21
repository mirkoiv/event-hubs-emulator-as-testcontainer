package example.testcontainers.eventhubemulator.docker;

import example.testcontainers.eventhubemulator.EmulatorProperties;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

abstract class AbstractEventHubContainerDockerCompose {
    private static final EmulatorProperties emulatorProperties = EmulatorProperties.get();

    private static final ComposeContainer environment = new ComposeContainer(
            new File(emulatorProperties.composerFile())
    )
            .withEnv(Map.of(
                    "ACCEPT_EULA", emulatorProperties.acceptEula(),
                    "CONFIG_FILE", emulatorProperties.configFile()
            ))
            .withExposedService("emulator", 5672,
                    Wait.forListeningPort().withStartupTimeout(Duration.of(10, ChronoUnit.SECONDS)))
            .withExposedService("azurite", 10000,
                    Wait.forListeningPort().withStartupTimeout(Duration.of(10, ChronoUnit.SECONDS)))
            .withExposedService("azurite", 10001,
                    Wait.forListeningPort().withStartupTimeout(Duration.of(10, ChronoUnit.SECONDS)))
            .withExposedService("azurite", 10002,
                    Wait.forListeningPort().withStartupTimeout(Duration.of(10, ChronoUnit.SECONDS)))
    ;

    static {
        environment.start();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.azure.eventhubs.connection-string=",
                () -> String.format("Endpoint=sb://localhost:%s;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;",
                        environment.getServicePort("emulator", 5672)));
        registry.add("spring.cloud.azure.eventhubs.processor.checkpoint-store.connection-string", () -> String.format(
                "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;" +
                        "BlobEndpoint=http://127.0.0.1:%s/devstoreaccount1;QueueEndpoint=http://127.0.0.1:%s/devstoreaccount1;TableEndpoint=http://127.0.0.1:%s/devstoreaccount1;",
                environment.getServicePort("azurite", 10000), environment.getServicePort("azurite", 10001), environment.getServicePort("azurite", 10002)
        ));
        registry.add("spring.cloud.azure.eventhubs.processor.checkpoint-store.endpoint", () -> String.format("http://localhost:%d/devstoreaccount1/", environment.getServicePort("azurite", 10000)));
    }
}
