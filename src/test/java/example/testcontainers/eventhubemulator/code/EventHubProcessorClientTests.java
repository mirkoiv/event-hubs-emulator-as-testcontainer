package example.testcontainers.eventhubemulator.code;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import example.testcontainers.eventhubemulator.Config;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("code-processor-client")
public class EventHubProcessorClientTests extends AbstractEventHubContainerCodeCompose {

    private static final Logger log = LoggerFactory.getLogger(EventHubProcessorClientTests.class);

    @Autowired
    private Config config;

    @Autowired
    private EventHubProducerClient producerClient;

    private BlobContainerClient blobContainerClient;
    private String containerName;

    @BeforeEach
    void setUp() {
        containerName = "test-" + UUID.randomUUID();
        blobContainerClient = new BlobContainerClientBuilder()
                .connectionString(config.azuriteConnectionString())
                .containerName(containerName)
                .buildClient();
        blobContainerClient.createIfNotExists();
    }

    @AfterEach
    void tearDown() {
        blobContainerClient.delete();
    }

    @Test
    void processorClientTest() {
        AtomicInteger counter = new AtomicInteger(0);
        Consumer<EventContext> partitionProcessor = eventContext -> {
            if (eventContext == null || eventContext.getEventData() == null) {
                return;
            }
            log.info("received event: " + eventContext.getEventData().getBodyAsString());
            counter.incrementAndGet();
            eventContext.updateCheckpoint();
        };

        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
                .connectionString(config.azuriteConnectionString())
                .containerName(containerName)
                .buildAsyncClient();

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .connectionString(config.emulatorConnectionString())
                .processEvent(partitionProcessor, Duration.ofSeconds(5))
                .processError(errorContext -> log.error("processing event error", errorContext.getThrowable()))
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
                .eventHubName(config.eventHub())
                .consumerGroup(config.consumerGroup())
                .initialPartitionEventPosition(s -> EventPosition.latest())
                .buildEventProcessorClient();

        eventProcessorClient.start();
        await().pollDelay(Duration.ofSeconds(5)).until(() -> true);

        producerClient.send(List.of(new EventData("Test event 1")));
        producerClient.send(List.of(new EventData("Test event 2")));

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                            assertThat(counter.get()).isEqualTo(2);
                        }
                );

        eventProcessorClient.stop();
        await().pollDelay(Duration.ofSeconds(5)).until(() -> true);
    }
}
