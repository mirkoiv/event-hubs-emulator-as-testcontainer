package example.testcontainers.eventhubemulator.code;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import example.testcontainers.eventhubemulator.TestStreamBinderConfiguration;
import example.testcontainers.eventhubemulator.Utils;
import example.testcontainers.eventhubemulator.fakes.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("code-stream-binder")
@Import(TestStreamBinderConfiguration.class)
public class EventHubStreamBinderTests extends AbstractEventHubContainerCodeCompose {

    @Autowired
    private EventHubProducerClient producerClient;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.clear();
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void streamConsumerTest() {
        producerClient.send(List.of(new EventData("Test event")));

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                            assertThat(eventRepository.count()).isEqualTo(1);
                            assertThat(eventRepository.get(0)).isEqualTo("Test event");
                        }
                );
    }
}
