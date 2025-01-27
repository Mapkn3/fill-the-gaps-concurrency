package course.concurrency.exams.auction;

public class Bid {
    private Long id;
    private Long participantId;
    private Long price;

    public Bid(Long id, Long participantId, Long price) {
        this.id = id;
        this.participantId = participantId;
        this.price = price;
    }

    public static Bid defaultBid() {
        return new Bid(-1L, -1L, -1L);
    }

    public Long getId() {
        return id;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public Long getPrice() {
        return price;
    }
}
