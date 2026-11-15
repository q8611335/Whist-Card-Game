package game.ai;

/** Enforces the budget of at most one LLM request per round across all AI players. */
public class RoundRequestLimiter {
    private int lastRequestedRound = -1;

    /** Returns true (and records the request) only for the first request of a round. */
    public synchronized boolean tryAcquire(int round) {
        if (lastRequestedRound == round) return false;
        lastRequestedRound = round;
        return true;
    }
}
