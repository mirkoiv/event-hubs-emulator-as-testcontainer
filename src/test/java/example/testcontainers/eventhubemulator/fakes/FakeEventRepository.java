package example.testcontainers.eventhubemulator.fakes;

import java.util.ArrayList;
import java.util.List;

public class FakeEventRepository implements EventRepository {
        private final List<String> list = new ArrayList<>();

        public void save(String event) {
            list.add(event);
        }

        public void clear() {
            list.clear();
        }

        public int count() {
            return list.size();
        }

        public String get(int index) {
            return list.get(index);
        }
    }