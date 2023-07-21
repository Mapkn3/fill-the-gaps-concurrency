package course.concurrency.m3_shared.immutable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static course.concurrency.m3_shared.immutable.Order.Status.DELIVERED;

public class OrderService {

    private final Map<Long, Order> currentOrders;
    private final AtomicLong nextId;

    public OrderService() {
        this.currentOrders = new ConcurrentHashMap<>();
        this.nextId = new AtomicLong(0L);
    }

    private long nextId() {
        return nextId.getAndIncrement();
    }

    public long createOrder(List<Item> items) {
        var id = nextId();
        var order = Order.init(id, items);
        currentOrders.put(id, order);
        return id;
    }

    public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
        var orderWithPaymentInfo = currentOrders.computeIfPresent(
                orderId,
                (ignore, order) -> order.withPaymentInfo(paymentInfo)
        );
        throwIfOrderIsNull(orderWithPaymentInfo);

        if (orderWithPaymentInfo.readyForDelivery()) {
            deliver(orderWithPaymentInfo);
        }
    }

    public void setPacked(long orderId) {
        var packedOrder = currentOrders.computeIfPresent(orderId, (ignore, order) -> order.packed());
        throwIfOrderIsNull(packedOrder);

        if (packedOrder.readyForDelivery()) {
            deliver(packedOrder);
        }
    }

    private void deliver(Order order) {
        /* ... */
        throwIfOrderIsNull(order);
        currentOrders.computeIfPresent(
                order.getId(),
                (ignore, orderToDelivery) ->
                        orderToDelivery.hasStatus(DELIVERED) ? orderToDelivery : orderToDelivery.withStatus(DELIVERED)
        );

    }

    public boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).hasStatus(DELIVERED);
    }

    private void throwIfOrderIsNull(Order order) {
        if (order == null) {
            throw new IllegalStateException("An order not found.");
        }
    }
}
