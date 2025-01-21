package example.testcontainers.eventhubemulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventHubEmulatorAsTestContainer {

    public static void main(String[] args) {
        SpringApplication.run(EventHubEmulatorAsTestContainer.class, args);
    }

}
