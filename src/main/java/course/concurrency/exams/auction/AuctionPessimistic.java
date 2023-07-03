package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {

    private final Notifier notifier;
    private final Object latestBidMonitor = new Object();
    private volatile Bid latestBid;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
        this.latestBid = Bid.defaultBid();
    }

    public boolean propose(Bid bid) {
        if (bid.getPrice() > latestBid.getPrice()) {
            Bid outdatedBid = null;
            synchronized (latestBidMonitor) {
                if (bid.getPrice() > latestBid.getPrice()) {
                    outdatedBid = latestBid;
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
}
