package course.concurrency.m3_shared.collections;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

import static java.util.stream.Collectors.toSet;

public class RestaurantService {

    private final Map<String, Restaurant> restaurantMap = new ConcurrentHashMap<>() {{
        put("A", new Restaurant("A"));
        put("B", new Restaurant("B"));
        put("C", new Restaurant("C"));
    }};

    private final ConcurrentMap<String, LongAdder> stat;

    public RestaurantService() {
        stat = new ConcurrentHashMap<>();
    }

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return restaurantMap.get(restaurantName);
    }

    public void addToStat(String restaurantName) {
        stat.computeIfAbsent(restaurantName, ignore -> new LongAdder()).increment();
    }

    public Set<String> printStat() {
        return stat.entrySet()
                .stream()
                .map(entry -> entry.getKey() + " - " + entry.getValue().sum())
                .collect(toSet());
    }
}
