package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private final Notifier notifier;
    private final AtomicMarkableReference<Bid> latestBidReference;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
        this.latestBidReference = new AtomicMarkableReference<>(Bid.defaultBid(), false);
    }

    public boolean propose(Bid bid) {
        Bid latestBid;
        do {
            latestBid = latestBidReference.getReference();
            if ((bid.getPrice() <= latestBid.getPrice()) || latestBidReference.isMarked()) {
                return false;
            }
        } while (!latestBidReference.compareAndSet(latestBid, bid, false, false));

        notifier.sendOutdatedMessage(latestBid);

        return true;
    }

    public Bid getLatestBid() {
        return latestBidReference.getReference();
    }

    public Bid stopAuction() {
        Bid latestBid;
        do {
            latestBid = latestBidReference.getReference();
        } while (!latestBidReference.attemptMark(latestBid, true));
        return latestBid;
    }
}
