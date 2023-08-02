package course.concurrency.exams.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Others {

    public static class LoadingCache<K, V> {
        Map<K, V> map = new ConcurrentHashMap<>();

        public void add(K key, V value) {
            map.put(key, value);
        }

        public void invalidate(K address) {
            map.remove(address);
        }

        public void cleanUp() {
            map.clear();
        }

    }

    public static class RouterClient {

    }

    public static class RouterState {
        private static final AtomicInteger counter = new AtomicInteger(0);
        private final String adminAddress;

        public RouterState(String address) {
            this.adminAddress = address + counter.incrementAndGet();
        }

        public String getAdminAddress() {
            return adminAddress;
        }
    }

    public static class RouterStore {
        List<RouterState> states = new ArrayList<>();

        public List<RouterState> getCachedRecords() {
            return states;
        }
    }

    public static class MountTableManager {

        private final String address;
        public MountTableManager(String address) {
            this.address = address;
        }

        public boolean refresh() {
            return ThreadLocalRandom.current().nextBoolean();
        }
    }

    public static class AddressValidator {
        public static boolean validate(String address) {
            return address != null && !address.isBlank();
        }
    }

    public static class AddressResolver {
        public static String resolve(String address) {
            return isLocalAdmin(address) ? "local" : address;
        }

        public static boolean isLocalAdmin(String address) {
            return address.contains("local");
        }
    }

    public static class MountTableManagerBuilder {
        public MountTableManager build(String address) {
            String resolvedAddress = AddressResolver.resolve(address);
            return new MountTableManager(resolvedAddress);
        }
    }
}
