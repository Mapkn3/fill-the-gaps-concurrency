package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private final Notifier notifier;
    private final AtomicReference<Bid> latestBidReference;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
        this.latestBidReference = new AtomicReference<>();
    }

    public boolean propose(Bid bid)  {
        if (latestBidReference.get() == null) {
            return latestBidReference.compareAndSet(null, bid) || propose(bid);
        }

        boolean bidIsGreaterThanLatest;
        Bid latestBid;
        do {
            latestBid = latestBidReference.get();
            bidIsGreaterThanLatest = bid.getPrice() > latestBid.getPrice();
        } while (bidIsGreaterThanLatest && !latestBidReference.compareAndSet(latestBid, bid));

        if (bidIsGreaterThanLatest) {
            notifier.sendOutdatedMessage(latestBid);
        }

        return bidIsGreaterThanLatest;
    }

    public Bid getLatestBid() {
        return latestBidReference.get();
    }
}
