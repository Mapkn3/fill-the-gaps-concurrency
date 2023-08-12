package course.concurrency.exams.refactoring;

import course.concurrency.exams.refactoring.Others.AddressValidator;
import course.concurrency.exams.refactoring.Others.LoadingCache;
import course.concurrency.exams.refactoring.Others.MountTableManager;
import course.concurrency.exams.refactoring.Others.MountTableManagerBuilder;
import course.concurrency.exams.refactoring.Others.RouterClient;
import course.concurrency.exams.refactoring.Others.RouterState;
import course.concurrency.exams.refactoring.Others.RouterStore;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;


public class MountTableRefresherService {

    private final ExecutorService executorService;
    private RouterStore routerStore = new RouterStore();
    private long cacheUpdateTimeout;
    /**
     * All router admin clients cached. So no need to create the client again and
     * again. Router admin address(host:port) is used as key to cache RouterClient
     * objects.
     */
    private LoadingCache<String, RouterClient> routerClientsCache;
    /**
     * Removes expired RouterClient from routerClientsCache.
     */
    private ScheduledExecutorService clientCacheCleanerScheduler;
    private MountTableManagerBuilder mountTableManagerBuilder;

    public MountTableRefresherService() {
        ThreadFactory threadFactory = r -> {
            Thread t = new Thread(r);
            if (r instanceof MountTableRefresher) {
                t.setName("MountTableRefresh_" + ((MountTableRefresher) r).getAdminAddress());
                t.setDaemon(true);
            }
            return t;
        };
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
    }

    public void serviceInit() {
        long routerClientMaxLiveTime = 15L;
        this.cacheUpdateTimeout = 10L;
        routerClientsCache = new LoadingCache<>();
        routerStore.getCachedRecords().stream()
                .map(RouterState::getAdminAddress)
                .forEach(addr -> routerClientsCache.add(addr, new RouterClient()));

        initClientCacheCleaner(routerClientMaxLiveTime);
    }

    public void serviceStop() {
        clientCacheCleanerScheduler.shutdown();
        // remove and close all admin clients
        routerClientsCache.cleanUp();
        executorService.shutdown();
    }

    private void initClientCacheCleaner(long routerClientMaxLiveTime) {
        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setName("MountTableRefresh_ClientsCacheCleaner");
            t.setDaemon(true);
            return t;
        };

        clientCacheCleanerScheduler = Executors.newSingleThreadScheduledExecutor(tf);
        /*
         * When cleanUp() method is called, expired RouterClient will be removed and
         * closed.
         */
        clientCacheCleanerScheduler.scheduleWithFixedDelay(
                () -> routerClientsCache.cleanUp(),
                routerClientMaxLiveTime,
                routerClientMaxLiveTime,
                MILLISECONDS
        );
    }

    /**
     * Refresh mount table cache of this router as well as all other routers.
     */
    public void refresh() {
        invokeRefresh(
                routerStore.getCachedRecords().stream()
                        .map(RouterState::getAdminAddress)
                        .filter(AddressValidator::validate)
                        .map(this::buildMountTableRefresher)
                        .collect(toList())
        ).join();
    }

    private MountTableRefresher buildMountTableRefresher(String adminAddress) {
        MountTableManager mountTableManager = mountTableManagerBuilder.build(adminAddress);
        return new MountTableRefresher(mountTableManager, adminAddress);
    }

    private void removeFromCache(String adminAddress) {
        routerClientsCache.invalidate(adminAddress);
    }

    private CompletableFuture<Void> invokeRefresh(List<MountTableRefresher> refreshers) {
        return CompletableFuture.allOf(
                        refreshers.stream()
                                .map(refresher -> runAsync(refresher, executorService)
                                        .orTimeout(cacheUpdateTimeout, MILLISECONDS))
                                .toArray(CompletableFuture[]::new)
                )
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause();
                    if (cause instanceof TimeoutException) {
                        log("Not all router admins updated their cache");
                    }
                    if (cause instanceof InterruptedException) {
                        log("Mount table cache refresher was interrupted.");
                    }
                    return null;
                })
                .thenRun(() -> logResult(refreshers));
    }

    private void logResult(List<MountTableRefresher> refreshThreads) {
        int successCount = 0;
        int failureCount = 0;
        for (MountTableRefresher mountTableRefreshThread : refreshThreads) {
            if (mountTableRefreshThread.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
                // remove RouterClient from cache so that new client is created
                removeFromCache(mountTableRefreshThread.getAdminAddress());
            }
        }
        log(String.format(
                "Mount table entries cache refresh successCount=%d,failureCount=%d",
                successCount, failureCount
        ));
    }

    public void log(String message) {
        System.out.println(message);
    }

    public void setCacheUpdateTimeout(long cacheUpdateTimeout) {
        this.cacheUpdateTimeout = cacheUpdateTimeout;
    }

    public void setRouterClientsCache(LoadingCache<String, RouterClient> cache) {
        this.routerClientsCache = cache;
    }

    public void setRouterStore(RouterStore routerStore) {
        this.routerStore = routerStore;
    }

    public void setMountTableManagerBuilder(MountTableManagerBuilder mountTableManagerBuilder) {
        this.mountTableManagerBuilder = mountTableManagerBuilder;
    }
}