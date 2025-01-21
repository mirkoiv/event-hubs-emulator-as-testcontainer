package example.testcontainers.eventhubemulator.fakes;

public interface EventRepository {
        void save(String event);
        void clear();
        int count();
        String get(int index);
    }