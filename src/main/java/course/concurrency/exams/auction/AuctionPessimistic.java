package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {

    private final Notifier notifier;
    private final Object latestBidMonitor = new Object();
    private Bid latestBid;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    public boolean propose(Bid bid) {
        synchronized (latestBidMonitor) {
            if (latestBid == null) {
                latestBid = bid;
                return true;
            }

            if (bid.getPrice() > latestBid.getPrice()) {
                notifier.sendOutdatedMessage(latestBid);
                latestBid = bid;
                return true;
            }
            return false;
        }
    }

    public Bid getLatestBid() {
        synchronized (latestBidMonitor) {
            return latestBid;
        }
    }
}
