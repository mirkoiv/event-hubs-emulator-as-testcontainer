package example.testcontainers.eventhubemulator;

import com.azure.spring.messaging.checkpoint.Checkpointer;
import example.testcontainers.eventhubemulator.fakes.EventRepository;
import example.testcontainers.eventhubemulator.fakes.FakeEventRepository;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

import static com.azure.spring.messaging.AzureHeaders.CHECKPOINTER;

@TestConfiguration
public class TestStreamBinderConfiguration {
    @Bean
    public EventRepository eventRepository() {
        return new FakeEventRepository();
    }

    @Bean
    public Consumer<Message<String>> consume(EventRepository eventRepository) {
        return message -> {
            eventRepository.save(message.getPayload());
            LoggerFactory.getLogger("consumer").info("event: " + message.getPayload());
            Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
            checkpointer.success().block();
        };
    }
}