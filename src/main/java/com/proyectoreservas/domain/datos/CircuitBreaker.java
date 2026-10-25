package com.proyectoreservas.domain.datos;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
public class CircuitBreaker {
    public enum State { CLOSED, OPEN, HALF_OPEN }
    private final int threshold;
    private final Duration resetTimeout;
    private final Clock clock;
    private int failures;
    private Instant openedAt;
    private State state = State.CLOSED;
    private boolean probeInProgress;
    public CircuitBreaker(int threshold, Duration resetTimeout, Clock clock) {
        if (threshold < 1) throw new IllegalArgumentException("threshold");
        this.threshold=threshold; this.resetTimeout=resetTimeout; this.clock=clock;
    }
    public synchronized State state() {
        if (state == State.OPEN && !clock.instant().isBefore(openedAt.plus(resetTimeout))) state=State.HALF_OPEN;
        return state;
    }
    public <T> T execute(Callable<T> work) throws Exception {
        synchronized (this) {
            State current=state();
            if (current == State.OPEN || (current == State.HALF_OPEN && probeInProgress)) throw new IllegalStateException("Circuit breaker OPEN");
            if (current == State.HALF_OPEN) probeInProgress=true;
        }
        try {
            T result=work.call();
            synchronized(this) { failures=0; state=State.CLOSED; probeInProgress=false; }
            return result;
        } catch (Exception e) {
            synchronized(this) {
                probeInProgress=false;
                if (state==State.HALF_OPEN || ++failures >=threshold) { state=State.OPEN; openedAt=clock.instant(); }
            }
            throw e;
        }
    }
}
