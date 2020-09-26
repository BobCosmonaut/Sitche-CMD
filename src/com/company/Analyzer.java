package com.company;

import java.util.concurrent.CountDownLatch;

public class Analyzer implements Runnable {

    Position position;
    Position parent;
    CountDownLatch latch;

    public Analyzer(Position position, Position parent, CountDownLatch latch) {
        this.position = position;
        this.parent = parent;
        this.latch = latch;
    }

    @Override
    public void run() {
        Position outcome = position.getBestOutcome();
        latch.countDown();
        parent.challengeBestOutcome(outcome, position);
    }
}
