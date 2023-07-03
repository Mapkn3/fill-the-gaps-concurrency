package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private final Notifier notifier;
    private final Object latestBidMonitor = new Object();
    private volatile Bid latestBid;
    private volatile boolean auctionHasBeenStopped;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
        this.latestBid = Bid.defaultBid();
        this.auctionHasBeenStopped = false;
    }

    public boolean propose(Bid bid) {
        if (bid.getPrice() > latestBid.getPrice()) {
            Bid outdatedBid = null;
            synchronized (latestBidMonitor) {
                if (bid.getPrice() > latestBid.getPrice()) {
                    outdatedBid = latestBid;
                    if (auctionHasBeenStopped) {
                        return false;
                    }
                    latestBid = bid;
                }
            }
            if (outdatedBid != null) {
                notifier.sendOutdatedMessage(outdatedBid);
                return true;
            }
        }

        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }

    public Bid stopAuction() {
        if (auctionHasBeenStopped) {
            return latestBid;
        }

        auctionHasBeenStopped = true;
        synchronized (latestBidMonitor) {
            return latestBid;
        }
    }
}
