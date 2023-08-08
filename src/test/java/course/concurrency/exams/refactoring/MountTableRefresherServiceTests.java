package course.concurrency.exams.refactoring;

import course.concurrency.exams.refactoring.Others.MountTableManager;
import course.concurrency.exams.refactoring.Others.MountTableManagerBuilder;
import course.concurrency.exams.refactoring.Others.RouterState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MountTableRefresherServiceTests {

    private static final long LONG_DELAY = 10_000L;
    private MountTableRefresherService service;
    @Mock
    private Others.RouterStore routerStore;
    @Mock
    private MountTableManager manager;
    @Mock
    private Others.LoadingCache<String, Others.RouterClient> routerClientsCache;
    @Mock
    private MountTableManagerBuilder mountTableManagerBuilder;

    @BeforeEach
    public void setUpStreams() {
        service = new MountTableRefresherService();
        service.setCacheUpdateTimeout(1000);
        service.setRouterStore(routerStore);
        service.setRouterClientsCache(routerClientsCache);
        service.setMountTableManagerBuilder(mountTableManagerBuilder);
        // service.serviceInit(); // needed for complex class testing, not for now
    }

    @AfterEach
    public void restoreStreams() {
        // service.serviceStop();
    }

    @Test
    @DisplayName("All tasks are completed successfully")
    public void allDone() {
        // given
        MountTableRefresherService mockedService = spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        when(manager.refresh()).thenReturn(true);

        List<RouterState> states = addresses.stream()
                .map(RouterState::new)
                .collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        when(mountTableManagerBuilder.build(anyString())).thenReturn(manager);

        // when
        mockedService.refresh().join();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=4,failureCount=0");
        verify(routerClientsCache, never()).invalidate(anyString());
    }

    @Test
    @DisplayName("All tasks failed")
    public void noSuccessfulTasks() {
        // given
        MountTableRefresherService mockedService = spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        when(manager.refresh()).thenReturn(false);

        List<RouterState> states = addresses.stream()
                .map(RouterState::new)
                .collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        when(mountTableManagerBuilder.build(anyString())).thenReturn(manager);

        // when
        mockedService.refresh().join();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=0,failureCount=4");
        verify(routerClientsCache, times(4)).invalidate(anyString());
    }

    @Test
    @DisplayName("Some tasks failed")
    public void halfSucceedTasks() {
        // given
        MountTableRefresherService mockedService = spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        when(manager.refresh()).thenReturn(true, false, true, false);

        List<RouterState> states = addresses.stream()
                .map(RouterState::new)
                .collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        when(mountTableManagerBuilder.build(anyString())).thenReturn(manager);

        // when
        mockedService.refresh().join();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=2,failureCount=2");
        verify(routerClientsCache, times(2)).invalidate(anyString());
    }

    @Test
    @DisplayName("One task completed with exception")
    public void exceptionInOneTask() {
        // given
        MountTableRefresherService mockedService = spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        when(manager.refresh()).thenReturn(true, true, true, false);

        List<RouterState> states = addresses.stream()
                .map(RouterState::new)
                .collect(toList());
        when(routerStore.getCachedRecords())
                .thenReturn(states);
        when(mountTableManagerBuilder.build(anyString())).thenReturn(manager);

        // when
        mockedService.refresh().join();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=3,failureCount=1");
        verify(routerClientsCache).invalidate(eq("local4"));
    }

    @Test
    @DisplayName("One task exceeds timeout")
    public void oneTaskExceedTimeout() {
        // given
        MountTableRefresherService mockedService = spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        when(manager.refresh()).thenReturn(true, true, true)
                .thenAnswer(invocation -> {
                    Thread.sleep(LONG_DELAY);
                    return false;
                });

        List<RouterState> states = addresses.stream()
                .map(RouterState::new)
                .collect(toList());
        when(routerStore.getCachedRecords())
                .thenReturn(states);
        when(mountTableManagerBuilder.build(anyString())).thenReturn(manager);

        // when
        mockedService.refresh().join();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=3,failureCount=1");
        verify(mockedService).log("Not all router admins updated their cache");
        verify(routerClientsCache).invalidate(anyString());
    }
}
