package course.concurrency.exams.refactoring;

import static course.concurrency.exams.refactoring.Others.MountTableManager;

public class MountTableRefresher implements Runnable {

    private boolean success;
    /** Admin server on which refreshed to be invoked. */
    private final String adminAddress;
    private final MountTableManager manager;

    public MountTableRefresher(MountTableManager manager,
                               String adminAddress) {
        this.manager = manager;
        this.adminAddress = adminAddress;
    }

    /**
     * Refresh mount table cache of local and remote routers. Local and remote
     * routers will be refreshed differently. Let's understand what are the
     * local and remote routers and refresh will be done differently on these
     * routers. Suppose there are three routers R1, R2 and R3. User want to add
     * new mount table entry. He will connect to only one router, not all the
     * routers. Suppose He connects to R1 and calls add mount table entry through
     * API or CLI. Now in this context R1 is local router, R2 and R3 are remote
     * routers. Because add mount table entry is invoked on R1, R1 will update the
     * cache locally it need not make RPC call. But R1 will make RPC calls to
     * update cache on R2 and R3.
     */
    @Override
    public void run() {
        success = manager.refresh();
    }

    /**
     * @return true if cache was refreshed successfully.
     */
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "MountTableRefresher [success=" + success + ", adminAddress=" + adminAddress + "]";
    }

    public String getAdminAddress() {
        return adminAddress;
    }
}
