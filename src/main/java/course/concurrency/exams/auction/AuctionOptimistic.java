package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private final Notifier notifier;
    private final AtomicReference<Bid> latestBidReference;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
        this.latestBidReference = new AtomicReference<>(Bid.defaultBid());
    }

    public boolean propose(Bid bid) {
        Bid latestBid;
        do {
            latestBid = latestBidReference.get();
            if (bid.getPrice() <= latestBid.getPrice()) {
                return false;
            }
        } while (!latestBidReference.compareAndSet(latestBid, bid));

        notifier.sendOutdatedMessage(latestBid);

        return true;
    }

    public Bid getLatestBid() {
        return latestBidReference.get();
    }
}
