package course.concurrency.m2_async.cf;

import course.concurrency.m2_async.cf.report.ReportServiceExecutors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.concurrent.Executors.*;

@Disabled
public class ParameterizedReportServiceTests {

    private static  ExecutorServiceTestsReport report;

    @BeforeAll
    static void beforeAll() {
        report = new ExecutorServiceTestsReport();
    }

    @AfterAll
    static void afterAll() {
        System.out.println(report);
    }

    public static Stream<Arguments> parameterizedTestMultipleTasksArgs() {
        var availableProcessors = Runtime.getRuntime().availableProcessors();
        return Stream.of(
                Arguments.of(
                        "newSingleThreadExecutor()",
                        newSingleThreadExecutor()
                ),
                Arguments.of(
                        "newCachedThreadPool()",
                        newCachedThreadPool()
                ),
                Arguments.of(
                        "newFixedThreadPool(1)",
                        newFixedThreadPool(1)
                ),
                Arguments.of(
                        "newFixedThreadPool(2)",
                        newFixedThreadPool(2)
                ),
                Arguments.of(
                        "newFixedThreadPool(3)",
                        newFixedThreadPool(3)
                ),
                Arguments.of(
                        "newFixedThreadPool(10)",
                        newFixedThreadPool(10)
                ),
                Arguments.of(
                        "newFixedThreadPool(" + (availableProcessors * 3) + ")",
                        newFixedThreadPool(availableProcessors * 3)
                ),
                Arguments.of(
                        "newFixedThreadPool(100)",
                        newFixedThreadPool(100)
                )
        );
    }

    @MethodSource("parameterizedTestMultipleTasksArgs")
    @ParameterizedTest(name = "ExecutorService: {0}")
    public void parameterizedTestMultipleTasks(String name, ExecutorService executor) throws InterruptedException {
        int poolSize = Runtime.getRuntime().availableProcessors() * 3;
        int iterations = 5;

        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);

        ReportServiceExecutors reportService = new ReportServiceExecutors(executor);
        for (int i = 0; i < poolSize; i++) {
            executorService.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException ignored) {}
                for (int it = 0; it < iterations; it++) {
                    reportService.getReport();
                }
            });
        }

        long start = System.currentTimeMillis();
        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        long end = System.currentTimeMillis();

        report.addRow(new ExecutorServiceTestsReport.ExecutorServiceTestsReportRow(name, Duration.ofMillis(end - start)));
    }

    private static final class ExecutorServiceTestsReport {
        private final List<ExecutorServiceTestsReportRow> report;

        private ExecutorServiceTestsReport() {
            report = new ArrayList<>();
        }

        private void addRow(ExecutorServiceTestsReportRow row) {
            report.add(row);
        }

        @Override
        public String toString() {
            var columnSeparator = "|";
            var rowSeparator = "-";
            var sb = new StringBuilder();
            sb.append(columnSeparator)
                    .append(String.format("%-26s", "ExecutorService"))
                    .append(columnSeparator)
                    .append(String.format("%-15s", "Execution time"))
                    .append(columnSeparator)
                    .append('\n')
                    .append(rowSeparator.repeat(44))
                    .append('\n');
            report.forEach(
                    row -> sb.append(columnSeparator)
                            .append(String.format("%26s", row.executorServiceName))
                            .append(columnSeparator)
                            .append(String.format("%15s", row.executionTime))
                            .append(columnSeparator)
                            .append('\n')
            );
            return sb.toString();
        }

        private static final class ExecutorServiceTestsReportRow {
            private final String executorServiceName;
            private final Duration executionTime;

            public ExecutorServiceTestsReportRow(String executorServiceName, Duration executionTime) {
                this.executorServiceName = executorServiceName;
                this.executionTime = executionTime;
            }
        }
    }
}
