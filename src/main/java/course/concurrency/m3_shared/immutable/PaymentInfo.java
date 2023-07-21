package course.concurrency.m3_shared.immutable;

public class PaymentInfo implements Cloneable {
    @Override
    public PaymentInfo clone() {
        try {
            return (PaymentInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
