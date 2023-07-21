package course.concurrency.m3_shared.immutable;

import java.util.List;
import java.util.Optional;

import static course.concurrency.m3_shared.immutable.Order.Status.IN_PROGRESS;
import static course.concurrency.m3_shared.immutable.Order.Status.NEW;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

public final class Order {

    private final Long id;
    private final List<Item> items;
    private PaymentInfo paymentInfo;
    private final boolean packed;
    private final Status status;

    private Order(Long id, List<Item> items, PaymentInfo paymentInfo, boolean packed, Status status) {
        this.id = id;
        this.items = freeze(requireNonNullElseGet(items, List::<Item>of));
        if (paymentInfo != null) {
            this.paymentInfo = paymentInfo.clone();
        }
        this.packed = packed;
        this.status = status;
    }

    public static Order init(Long id, List<Item> items) {
        return new Order(id, freeze(items), null, false, NEW);
    }

    public boolean readyForDelivery() {
        return items != null && !items.isEmpty() && paymentInfo != null && packed;
    }

    public Long getId() {
        return id;
    }

    public List<Item> getItems() {
        return requireNonNullElseGet(items, List::<Item>of)
                .stream()
                .map(Item::clone)
                .collect(toList());
    }

    public Optional<PaymentInfo> getPaymentInfo() {
        return Optional.ofNullable(paymentInfo)
                .map(PaymentInfo::clone);  // need deep clone if contains at least one non-primitive field
    }

    public Order withPaymentInfo(PaymentInfo paymentInfo) {
        return new Order(this.id, this.items, paymentInfo.clone(), this.packed, IN_PROGRESS);
    }

    public boolean isPacked() {
        return packed;
    }

    public Order packed() {
        return new Order(this.id, this.items, this.paymentInfo, true, IN_PROGRESS);
    }

    public Order unpacked() {
        return new Order(this.id, this.items, this.paymentInfo, false, IN_PROGRESS);
    }

    public boolean hasStatus(Status status) {
        return this.status == status;
    }

    public Order withStatus(Status status) {
        return new Order(this.id, this.items, this.paymentInfo, this.packed, status);
    }

    private static List<Item> freeze(List<Item> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(Item::clone)
                .collect(toUnmodifiableList());
    }

    public enum Status {
        NEW,
        IN_PROGRESS,
        DELIVERED,
    }
}
