package course.concurrency.m3_shared.immutable;

public class Item implements Cloneable {
    @Override
    public Item clone() {
        try {
            return (Item) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
