package example.testcontainers.eventhubemulator.code;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("code-partition-consumer")
class EventHubPartitionConsumerTests extends AbstractEventHubContainerCodeCompose {
    @Autowired
    private EventHubProducerClient producerClient;

    @Autowired
    private EventHubConsumerClient consumerClient;

    @Test
    void partitionConsumerTest() {
        String PARTITION_ID = "0";
        SendOptions sendOptions = new SendOptions();
        sendOptions.setPartitionId(PARTITION_ID);

        IterableStream<PartitionEvent> partitionEvents = consumerClient.receiveFromPartition(PARTITION_ID, 1,
                EventPosition.latest(), Duration.of(5, ChronoUnit.SECONDS));
        Iterator<PartitionEvent> iterator = partitionEvents.stream().iterator();

        producerClient.send(List.of(new EventData("Test event")), sendOptions);

        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().getData().getBodyAsString()).isEqualTo("Test event");

        partitionEvents.stream().close();
    }

}
