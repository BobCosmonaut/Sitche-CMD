package com.company;

import java.util.concurrent.CountDownLatch;

public class Seeker implements Runnable {

    Position position;
    CountDownLatch latch;
    public Seeker(Position position, CountDownLatch latch){
        this.position = position;
        this.latch = latch;
    }

    @Override
    public void run() {
        position.findAllMoves(true);
        latch.countDown();
    }
}
