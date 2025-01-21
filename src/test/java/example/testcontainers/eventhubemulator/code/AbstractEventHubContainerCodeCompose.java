package example.testcontainers.eventhubemulator.code;

import example.testcontainers.eventhubemulator.EmulatorProperties;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

abstract class AbstractEventHubContainerCodeCompose {

    private static final EmulatorProperties emulatorProperties = EmulatorProperties.get();

    private static final Network network = Network.newNetwork();

    private static final GenericContainer<?> azurite = new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:latest"))
            .withNetwork(network)
            // if '--skipApiVersionCheck' is required
            //.withCommand("azurite --blobHost 0.0.0.0 --blobPort 10000 --queueHost 0.0.0.0 --queuePort 10001 --tableHost 0.0.0.0 --tablePort 10002 --skipApiVersionCheck")
            .withExposedPorts(10000, 10001, 10002)
            .withNetworkAliases("azurite");

    private static final GenericContainer<?> emulator = new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/azure-messaging/eventhubs-emulator:latest"))
            .withNetwork(network)
            .withEnv("ACCEPT_EULA", emulatorProperties.acceptEula())
            .withEnv("BLOB_SERVER", "azurite")
            .withEnv("METADATA_SERVER", "azurite")
            .dependsOn(azurite)
            .withExposedPorts(5672)
            .withFileSystemBind(emulatorProperties.configFile(), "/Eventhubs_Emulator/ConfigFiles/Config.json", BindMode.READ_ONLY)
            .withNetworkAliases("tc-eh-emulator");

    static {
        azurite.start();
        emulator.start();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.azure.eventhubs.connection-string=",
                () -> String.format("Endpoint=sb://localhost:%d;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;",
                        emulator.getFirstMappedPort()));
        registry.add("spring.cloud.azure.eventhubs.processor.checkpoint-store.connection-string", () -> String.format(
                "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;" +
                "BlobEndpoint=http://127.0.0.1:%d/devstoreaccount1;QueueEndpoint=http://127.0.0.1:%d/devstoreaccount1;TableEndpoint=http://127.0.0.1:%d/devstoreaccount1;",
                azurite.getMappedPort(10000), azurite.getMappedPort(10001), azurite.getMappedPort(10002)
        ));
        registry.add("spring.cloud.azure.eventhubs.processor.checkpoint-store.endpoint", () -> String.format("http://localhost:%d/devstoreaccount1/", azurite.getMappedPort(10000)));
    }
}
