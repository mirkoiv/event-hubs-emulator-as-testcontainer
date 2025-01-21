package example.testcontainers.eventhubemulator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record Config(
    @Value("${azure.eventhub}")
    String eventHub,

    @Value("${azure.consumer-group}")
    String consumerGroup,

    @Value("${spring.cloud.azure.eventhubs.processor.checkpoint-store.connection-string}")
    String azuriteConnectionString,

    @Value("${spring.cloud.azure.eventhubs.connection-string}")
    String emulatorConnectionString
) {

}
