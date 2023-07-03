package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private final Notifier notifier;
    private final AtomicReference<Bid> latestBidReference;
    private final AtomicBoolean atomicAuctionHasBeenStopped;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
        this.latestBidReference = new AtomicReference<>(Bid.defaultBid());
        this.atomicAuctionHasBeenStopped = new AtomicBoolean(false);
    }

    public boolean propose(Bid bid) {
        Bid latestBid;
        do {
            latestBid = latestBidReference.get();
            if ((bid.getPrice() <= latestBid.getPrice()) || atomicAuctionHasBeenStopped.get()) {
                return false;
            }
        } while (!latestBidReference.compareAndSet(latestBid, bid));

        notifier.sendOutdatedMessage(latestBid);

        return true;
    }

    public Bid getLatestBid() {
        return latestBidReference.get();
    }

    public Bid stopAuction() {
        atomicAuctionHasBeenStopped.compareAndSet(false, true);
        return latestBidReference.get();
    }
}
