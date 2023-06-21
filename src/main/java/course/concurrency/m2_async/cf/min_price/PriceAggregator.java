package course.concurrency.m2_async.cf.min_price;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

public class PriceAggregator {

    private static final Duration GET_PRICE_TIMEOUT = Duration.ofMillis(2900);
    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private PriceRetriever priceRetriever = new PriceRetriever();
    private Collection<Long> shopIds = Set.of(10L, 45L, 66L, 345L, 234L, 333L, 67L, 123L, 768L);

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        var priceFutures = this.shopIds.stream()
                .map(shopId -> buildPriceSupplier(itemId, shopId))
                .map(this::buildPriceCompletableFuture)
                .collect(toList());

        CompletableFuture.allOf(priceFutures.toArray(new CompletableFuture[0])).join();

        return priceFutures.stream()
                .map(CompletableFuture::join)
                .filter(Double::isFinite)
                .mapToDouble(price -> price)
                .min()
                .orElse(Double.NaN);
    }

    private Supplier<Double> buildPriceSupplier(long itemId, long shopId) {
        return () -> priceRetriever.getPrice(itemId, shopId);
    }

    private CompletableFuture<Double> buildPriceCompletableFuture(Supplier<Double> priceSupplier) {
        return CompletableFuture.supplyAsync(priceSupplier, cachedThreadPool)
                .exceptionally(throwable -> Double.NaN)
                .completeOnTimeout(Double.NaN, GET_PRICE_TIMEOUT.toMillis(), MILLISECONDS);
    }
}
