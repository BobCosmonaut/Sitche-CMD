package com.company;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

public class Opponent {
    boolean IAmWhite;
    public static RuleBook ruleBook;

    private boolean multithreading;
    private final DecimalFormat decimalFormat =   new DecimalFormat("###,###,###");;

    public Opponent(boolean white, boolean multithreading) {
        IAmWhite = white;
        ruleBook = new RuleBook();
        this.multithreading = multithreading;
    }

    public Position makeMove(Position initialPosition) {
        while (initialPosition.depth != 0) {
            initialPosition.shiftDepth();
        }

        long beginningTime = System.currentTimeMillis();

        if (multithreading) {
            initialPosition.findAllMoves(false);
            if (initialPosition.subpositons.size() > 0) {
                CountDownLatch latch = new CountDownLatch(initialPosition.subpositons.size());
                for (int i = 0; i < initialPosition.subpositons.size(); i++) {
                    Seeker seeker = new Seeker(initialPosition.subpositons.get(i), latch);
                    Thread thread = new Thread(seeker);
                    thread.start();
                }
                try {
                    latch.await();
                } catch (Exception e) {
                    System.out.println("Couldn't wait");
                }
            }
        } else {
            initialPosition.findAllMoves(true);
        }
        System.out.println("Done Searching after " + decimalFormat.format(System.currentTimeMillis() - beginningTime) + " milliseconds.");
        System.out.println(decimalFormat.format(Position.totalPositions) + " positions created by searching " + Position.maxSearchDepth + " moves ahead.");
        Position.totalPositions = 0;

        Position returnPosition = initialPosition.bestImmediateMove(multithreading);
        System.out.println("Moved after " + decimalFormat.format(System.currentTimeMillis() - beginningTime) + " milliseconds.");
        return returnPosition;
    }

    Position bestParent;
    Position bestOutcome;

    synchronized void challengeBestMove(Position outcome, Position parent) {
        if (bestOutcome == null || bestParent == null) {
            bestParent = parent;
            bestOutcome = outcome;
        }
    }
}