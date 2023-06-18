package course.concurrency.m2_async.cf;

import course.concurrency.m2_async.cf.report.ReportServiceExecutors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.concurrent.Executors.*;

public class ReportServiceParameterizedTests {

    //    private ReportServiceCF reportService = new ReportServiceCF();
    private ReportServiceExecutors reportService = new ReportServiceExecutors();
    private static ExecutorServiceTestsReport report;

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
                //<editor-fold desc="poolSize = availableProcessors">
                Arguments.of(
                        "newSingleThreadExecutor()",
                        newSingleThreadExecutor(),
                        availableProcessors
                ),
                Arguments.of(
                        "newCachedThreadPool()",
                        newCachedThreadPool(),
                        availableProcessors
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors / 2 + ")",
                        newFixedThreadPool(availableProcessors / 2),
                        availableProcessors
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors + ")",
                        newFixedThreadPool(availableProcessors),
                        availableProcessors
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors * 2 + ")",
                        newFixedThreadPool(availableProcessors * 2),
                        availableProcessors
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors * 3 + ")",
                        newFixedThreadPool(availableProcessors * 3),
                        availableProcessors
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors * 4 + ")",
                        newFixedThreadPool(availableProcessors * 4),
                        availableProcessors
                ),
                //</editor-fold>

                //<editor-fold desc="poolSize = availableProcessors * 2">
                Arguments.of(
                        "newSingleThreadExecutor()",
                        newSingleThreadExecutor(),
                        availableProcessors * 2
                ),
                Arguments.of(
                        "newCachedThreadPool()",
                        newCachedThreadPool(),
                        availableProcessors * 2
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors / 2 + ")",
                        newFixedThreadPool(availableProcessors / 2),
                        availableProcessors * 2
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors + ")",
                        newFixedThreadPool(availableProcessors),
                        availableProcessors * 2
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors * 2 + ")",
                        newFixedThreadPool(availableProcessors * 2),
                        availableProcessors * 2
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors * 3 + ")",
                        newFixedThreadPool(availableProcessors * 3),
                        availableProcessors * 2
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors * 4 + ")",
                        newFixedThreadPool(availableProcessors * 4),
                        availableProcessors * 2
                ),
                //</editor-fold>

                //<editor-fold desc="poolSize = availableProcessors * 3">
                Arguments.of(
                        "newSingleThreadExecutor()",
                        newSingleThreadExecutor(),
                        availableProcessors * 3
                ),
                Arguments.of(
                        "newCachedThreadPool()",
                        newCachedThreadPool(),
                        availableProcessors * 3
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors / 2 + ")",
                        newFixedThreadPool(availableProcessors / 2),
                        availableProcessors * 3
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors + ")",
                        newFixedThreadPool(availableProcessors),
                        availableProcessors * 3
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors * 2 + ")",
                        newFixedThreadPool(availableProcessors * 2),
                        availableProcessors * 3
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors * 3 + ")",
                        newFixedThreadPool(availableProcessors * 3),
                        availableProcessors * 3
                ),
                Arguments.of(
                        "newFixedThreadPool(" + availableProcessors * 4 + ")",
                        newFixedThreadPool(availableProcessors * 4),
                        availableProcessors * 3
                )
                //</editor-fold>
        );
    }

    @MethodSource("parameterizedTestMultipleTasksArgs")
    @ParameterizedTest(name = "ExecutorService: {0}; PoolSize: {2}")
    public void parameterizedTestMultipleTasks(String name, ExecutorService executor, int poolSize) throws InterruptedException {
        int iterations = 5;

        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < poolSize; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException ignored) {
                }
                for (int it = 0; it < iterations; it++) {
                    reportService.getReport();
                }
            });
        }

        long start = System.currentTimeMillis();
        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        long end = System.currentTimeMillis();

        report.addRow(new ExecutorServiceTestsReport.ExecutorServiceTestsReportRow(name, poolSize, Duration.ofMillis(end - start)));
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
                    .append(String.format("%-10s", "Pool size"))
                    .append(columnSeparator)
                    .append(String.format("%-15s", "Execution time"))
                    .append(columnSeparator)
                    .append('\n')
                    .append(rowSeparator.repeat(55))
                    .append('\n');
            report.forEach(
                    row -> sb.append(columnSeparator)
                            .append(String.format("%26s", row.executorServiceName))
                            .append(columnSeparator)
                            .append(String.format("%10d", row.poolSize))
                            .append(columnSeparator)
                            .append(String.format("%15s", row.executionTime))
                            .append(columnSeparator)
                            .append('\n')
            );
            return sb.toString();
        }

        private static final class ExecutorServiceTestsReportRow {
            private final String executorServiceName;
            private final int poolSize;
            private final Duration executionTime;

            public ExecutorServiceTestsReportRow(String executorServiceName, int poolSize, Duration executionTime) {
                this.executorServiceName = executorServiceName;
                this.poolSize = poolSize;
                this.executionTime = executionTime;
            }
        }
    }
}
