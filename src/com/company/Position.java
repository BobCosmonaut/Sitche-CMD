package com.company;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static com.company.Piece.*;

public class Position {
    static int maxSearchDepth = 5;
    static RuleBook rules = new RuleBook();
    static int totalPositions;

    byte[][] pieces;

    ArrayList<Position> subpositons;
    byte score;
    short positionalScore;
    boolean whiteToMove = true;
    byte depth;

    boolean blackLong = true, blackShort = true, whiteLong = true, whiteShort = true;

    public Position bestImmediateMove(boolean multiThreading) {
        if (subpositons.size() == 0) {
            return this;
        }

        if (!multiThreading) {
            Position bestMove = subpositons.get(0);
            int bestKnownScore = bestMove.getBestOutcome().score;
            if (whiteToMove) {
                for (int i = 1; i < subpositons.size(); i++) {
                    Position testPosition = subpositons.get(i).getBestOutcome();
                    int testingScore = testPosition.score;
                    if (testingScore > bestKnownScore) {
                        bestKnownScore = testingScore;
                        bestMove = subpositons.get(i);
                    } else if (testingScore == bestKnownScore) {
                        bestMove.scorePosition();
                        subpositons.get(i).scorePosition();
                        if (subpositons.get(i).positionalScore >= bestMove.positionalScore) {
                            bestMove = subpositons.get(i);
                        }
                    }
                }
            } else {
                for (int i = 1; i < subpositons.size(); i++) {
                    Position testPosition = subpositons.get(i).getBestOutcome();
                    int testingScore = testPosition.score;
                    if (testingScore < bestKnownScore) {
                        bestKnownScore = testingScore;
                        bestMove = subpositons.get(i);
                    } else if (testingScore == bestKnownScore) {
                        bestMove.scorePosition();
                        subpositons.get(i).scorePosition();
                        if (subpositons.get(i).positionalScore >= bestMove.positionalScore) {
                            bestMove = subpositons.get(i);
                        }
                    }
                }
            }
            return bestMove;
        } else {
            CountDownLatch latch = new CountDownLatch(subpositons.size());
            for (int i = 0; i < subpositons.size(); i++) {
                Analyzer analyzer = new Analyzer(subpositons.get(i), this, latch);
                Thread thread = new Thread(analyzer);
                thread.start();
            }
            try {
                latch.await();
            } catch (Exception e) {
                System.out.println("Couldn't wait for analysis.");
            }

            return bestParent;
        }
    }

    Position bestOutcome;
    Position bestParent;

    public synchronized void challengeBestOutcome(Position newOutcome, Position newParent) {
        if (bestOutcome == null) {
            bestOutcome = newOutcome;
            bestParent = newParent;
        } else {
            if (whiteToMove) {
                if (newOutcome.score > bestOutcome.score) {
                    bestOutcome = newOutcome;
                    bestParent = newParent;
                } else if (newOutcome.score == bestOutcome.score) {
                    bestParent.score();
                    newParent.score();
                    if (newParent.score >= bestParent.score) {
                        bestOutcome = newOutcome;
                        bestParent = newParent;
                    }
                }
            } else {
                if (newOutcome.score < bestOutcome.score) {
                    bestOutcome = newOutcome;
                    bestParent = newParent;
                } else if (newOutcome.score == bestOutcome.score) {
                    newOutcome.scorePosition();
                    bestOutcome.scorePosition();
                    if (newOutcome.positionalScore <= bestOutcome.positionalScore) {
                        bestOutcome = newOutcome;
                        bestParent = newParent;
                    }
                }
            }
        }

    }

    public Position getBestOutcome() {
        if (subpositons.size() == 0) {
            score();
            return this;
        } else {
            Position bestKnownOutcome = subpositons.get(0).getBestOutcome();
            Position testingPosition;
            if (whiteToMove) {
                for (int i = 1; i < subpositons.size(); i++) {
                    testingPosition = subpositons.get(i).getBestOutcome();
                    if (testingPosition.score > bestKnownOutcome.score) {
                        bestKnownOutcome = testingPosition;
                    }
                    // If the positions have equal material score
                    else if (bestKnownOutcome.score == testingPosition.score) {
                        bestKnownOutcome.scorePosition();
                        testingPosition.scorePosition();
                        if (testingPosition.positionalScore > bestKnownOutcome.positionalScore) {
                            bestKnownOutcome = testingPosition;
                        }
                    }
                }
            } else {
                for (int i = 1; i < subpositons.size(); i++) {
                    testingPosition = subpositons.get(i).getBestOutcome();
                    if (testingPosition.score < bestKnownOutcome.score) {
                        bestKnownOutcome = testingPosition;
                    }
                    // If the positions have equal material score
                    else if (bestKnownOutcome.score == testingPosition.score) {
                        bestKnownOutcome.scorePosition();
                        testingPosition.scorePosition();
                        if (testingPosition.positionalScore > bestKnownOutcome.positionalScore) {
                            bestKnownOutcome = testingPosition;
                        }
                    }
                }
            }
            if (depth == 0) {
                System.out.println(this.toString());
            }
            return bestKnownOutcome;
        }
    }

    public void findAllMoves(boolean recursively) {
        if (depth != Position.maxSearchDepth && subpositons.size() == 0) {
            determineLegalMoves();

            for (int i = 0; i < subpositons.size(); i++) {
                if (subpositons.get(i).kingIsInCheck(whiteToMove)) {
                    subpositons.remove(i);
                    i--;
                }
            }

            if (recursively) {
                for (int i = 0; i < subpositons.size(); i++) {
                    subpositons.get(i).findAllMoves(true);
                }
                if (depth == maxSearchDepth - 1) {
                    trimSubpositions();
                }
            }
        }
    }

    /**
     *
     * @return -128 if white is mated, 127 if black is mated, 0 for a normal position.
     */
    public int gameOver() {
        if(kingIsInCheck(true)){
            return Byte.MIN_VALUE;
        }

        if(kingIsInCheck(false)){
            return Byte.MAX_VALUE;
        }
        return 0;
    }


    public void score() {
        // If is an end game position...
        if (subpositons.size() == 0 && depth != maxSearchDepth) {
            if (kingIsInCheck(true)) {
                score = Byte.MIN_VALUE;
                return;
            } else if (kingIsInCheck(false)) {
                score = Byte.MAX_VALUE;
                return;
            } else {
                score = 0;
                return;
            }
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                byte piece = pieces[i][j];
                if (piece == wPawn.code) {
                    score += 1;
                } else if (piece == bPawn.code()) {
                    score -= 1;
                } else if (piece == wBishop.code() || piece == wKnight.code()) {
                    score += 3;
                } else if (piece == bBishop.code() || piece == bKnight.code()) {
                    score -= 3;
                } else if (piece == wRook.code()) {
                    score += 6;
                } else if (piece == bRook.code()) {
                    score -= 6;
                } else if (piece == wQueen.code()) {
                    score += 9;
                } else if (piece == bQueen.code()) {
                    score -= 9;
                }
            }
        }
    }

    public void scorePosition() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (pieceIsWhite(pieces[i][j]) == !whiteToMove && Math.abs(pieces[i][j]) > 1) {
                    for (int k = 0; k < 8; k++) {
                        for (int l = 0; l < 8; l++) {
                            if (rules.isMoveLegal(this, new Point(i, j),
                                    new Point(k, l), true)) {
                                positionalScore++;
                            }
                        }
                    }
                }
            }
        }
    }

    public Position(Position parent, int toX, int toY, int fromX, int fromY) {
        addToTotal();
        pieces = createNewLocations(parent.pieces, toX, toY, fromX, fromY);
        depth = parent.depth;
        depth += 1;
        whiteToMove = !parent.whiteToMove;
        subpositons = new ArrayList<>();
    }

    public Position() {
        pieces = new byte[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                pieces[i][j] = RuleBook.initialPositionB[j][i];
            }
        }
        subpositons = new ArrayList<>();
    }

    private void trimSubpositions() {
        while (subpositons.size() > 1) {
            if (!whiteToMove) {
                if (subpositons.get(1).score > subpositons.get(0).score) {
                    subpositons.remove(0);
                } else if (subpositons.get(1).positionalScore > subpositons.get(0).positionalScore) {
                    subpositons.remove(0);
                } else {
                    subpositons.remove(1);
                }
            } else {
                if (subpositons.get(1).score < subpositons.get(0).score) {
                    subpositons.remove(0);
                } else if (subpositons.get(1).positionalScore < subpositons.get(0).positionalScore) {
                    subpositons.remove(0);
                } else {
                    subpositons.remove(1);
                }
            }
        }
    }

    byte[][] createNewLocations(byte[][] pieces, int x, int y, int dX, int dY) {
        byte[][] newPieces = new byte[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                newPieces[i][j] = pieces[i][j];
            }
        }
        newPieces[dX][dY] = pieces[x][y];
        newPieces[x][y] = empty.code();

        return newPieces;
    }

    void determineLegalMoves() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                byte piece = pieces[i][j];
                if ((pieceIsWhite(piece) == whiteToMove) && piece != empty.code()) {
                    if (piece == bRook.code() || piece == wRook.code()) {
                        findRookMoves(i, j);
                    } else if (piece == bBishop.code() || piece == wBishop.code()) {
                        findBishopMoves(i, j);
                    } else if (piece == wQueen.code() || piece == bQueen.code()) {
                        findBishopMoves(i, j);
                        findRookMoves(i, j);
                    } else if (piece == bKnight.code() || piece == wKnight.code()) {
                        findKnightMoves(i, j);
                    } else if (piece == wKing.code() || piece == bKing.code()) {
                        findKingMoves(i, j);
                    } else if (piece == wPawn.code()) {
                        findWhitePawnMoves(i, j);
                    } else if (piece == bPawn.code()) {
                        findBlackPawnMoves(i, j);
                    }
                }
            }
        }
    }

    void findBishopMoves(int x, int y) {
        for (int i = 1; i < 8; i++) {
            if (squareExists(x + i, y + i)) {
                if (isLandable(pieces[x + i][y + i])) {
                    subpositons.add(new Position(this, x, y, x + i, y + i));
                    // This prevents the piece from jumping over other pieces
                    if (pieces[x + i][y + i] != empty.code()) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        for (int i = 1; i < 8; i++) {
            if (squareExists(x + i, y - i)) {
                if (isLandable(pieces[x + i][y - i])) {
                    subpositons.add(new Position(this, x, y, x + i, y - i));
                    // This prevents the piece from jumping over other pieces
                    if (pieces[x + i][y - i] != empty.code()) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        for (int i = 1; i < 8; i++) {
            if (squareExists(x - i, y + i)) {
                if (isLandable(pieces[x - i][y + i])) {
                    subpositons.add(new Position(this, x, y, x - i, y + i));
                    // This prevents the piece from jumping over other pieces
                    if (pieces[x - i][y + i] != empty.code()) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        for (int i = 1; i < 8; i++) {
            if (squareExists(x - i, y - i)) {
                if (isLandable(pieces[x - i][y - i])) {
                    subpositons.add(new Position(this, x, y, x - i, y - i));

                    // This prevents the piece from jumping over other pieces
                    if (pieces[x - i][y - i] != empty.code()) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    void findRookMoves(int x, int y) {
        for (int i = 1; i < 8; i++) {
            if (squareExists(x + i, y)) {
                if (isLandable(pieces[x + i][y])) {
                    subpositons.add(new Position(this, x, y, x + i, y));
                    // This prevents the piece from jumping over other pieces
                    if (pieces[x + i][y] != empty.code()) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        for (int i = 1; i < 8; i++) {
            if (squareExists(x - i, y)) {
                if (isLandable(pieces[x - i][y])) {
                    subpositons.add(new Position(this, x, y, x - i, y));

                    // This prevents the piece from jumping over other pieces
                    if (pieces[x - i][y] != empty.code()) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        for (int i = 1; i < 8; i++) {
            if (squareExists(x, y + i)) {
                if (isLandable(pieces[x][y + i])) {
                    subpositons.add(new Position(this, x, y, x, y + i));
                    // This prevents the piece from jumping over other pieces
                    if (pieces[x][y + i] != empty.code()) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        for (int i = 1; i < 8; i++) {
            if (squareExists(x, y - i)) {
                if (isLandable(pieces[x][y - i])) {
                    subpositons.add(new Position(this, x, y, x, y - i));

                    // This prevents the piece from jumping over other pieces
                    if (pieces[x][y - i] != empty.code()) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    void findKnightMoves(int x, int y) {
        if (squareExists(x + 2, y + 1)) {
            if (isLandable(pieces[x + 2][y + 1])) {
                subpositons.add(new Position(this, x, y, x + 2, y + 1));
            }
        }
        if (squareExists(x + 2, y - 1)) {
            if (isLandable(pieces[x + 2][y - 1])) {
                subpositons.add(new Position(this, x, y, x + 2, y - 1));
            }
        }
        if (squareExists(x - 2, y + 1)) {
            if (isLandable(pieces[x - 2][y + 1])) {
                subpositons.add(new Position(this, x, y, x - 2, y + 1));
            }
        }
        if (squareExists(x - 2, y - 1)) {
            if (isLandable(pieces[x - 2][y - 1])) {
                subpositons.add(new Position(this, x, y, x - 2, y - 1));
            }
        }
        if (squareExists(x + 1, y + 2)) {
            if (isLandable(pieces[x + 1][y + 2])) {
                subpositons.add(new Position(this, x, y, x + 1, y + 2));
            }
        }
        if (squareExists(x + 1, y - 2)) {
            if (isLandable(pieces[x + 1][y - 2])) {
                subpositons.add(new Position(this, x, y, x + 1, y - 2));
            }
        }
        if (squareExists(x - 1, y + 2)) {
            if (isLandable(pieces[x - 1][y + 2])) {
                subpositons.add(new Position(this, x, y, x - 1, y + 2));
            }
        }
        if (squareExists(x - 1, y - 2)) {
            if (isLandable(pieces[x - 1][y - 2])) {
                subpositons.add(new Position(this, x, y, x - 1, y - 2));
            }
        }
    }

    void findKingMoves(int x, int y) {
        if (squareExists(x + 1, y)) {
            if (isLandable(pieces[x + 1][y])) {
                subpositons.add(new Position(this, x, y, x + 1, y));
            }
        }
        if (squareExists(x + 1, y - 1)) {
            if (isLandable(pieces[x + 1][y - 1])) {
                subpositons.add(new Position(this, x, y, x + 1, y - 1));
            }
        }
        if (squareExists(x + 1, y + 1)) {
            if (isLandable(pieces[x + 1][y + 1])) {
                subpositons.add(new Position(this, x, y, x + 1, y + 1));
            }
        }
        if (squareExists(x, y + 1)) {
            if (isLandable(pieces[x][y + 1])) {
                subpositons.add(new Position(this, x, y, x, y + 1));
            }
        }
        if (squareExists(x, y - 1)) {
            if (isLandable(pieces[x][y - 1])) {
                subpositons.add(new Position(this, x, y, x, y - 1));
            }
        }
        if (squareExists(x - 1, y)) {
            if (isLandable(pieces[x - 1][y])) {
                subpositons.add(new Position(this, x, y, x - 1, y));
            }
        }
        if (squareExists(x - 1, y + 1)) {
            if (isLandable(pieces[x - 1][y + 1])) {
                subpositons.add(new Position(this, x, y, x - 1, y + 1));
            }
        }
        if (squareExists(x - 1, y - 1)) {
            if (isLandable(pieces[x - 1][y - 1])) {
                subpositons.add(new Position(this, x, y, x - 1, y - 1));
            }
        }
    }

    void findWhitePawnMoves(int x, int y) {
        if (squareExists(x, y + 1)) {
            if (pieces[x][y + 1] == empty.code()) {
                subpositons.add(new Position(this, x, y, x, y + 1));
            }
        }

        if (squareExists(x + 1, y + 1)) {
            if (pieces[x + 1][y + 1] != empty.code()
                    && pieceIsWhite(pieces[x + 1][y + 1]) != whiteToMove) {
                subpositons.add(new Position(this, x, y, x + 1, y + 1));
            }
        }
        if (squareExists(x - 1, y + 1)) {
            if (pieces[x - 1][y + 1] != empty.code()
                    && pieceIsWhite(pieces[x - 1][y + 1]) != whiteToMove) {
                subpositons.add(new Position(this, x, y, x - 1, y + 1));
            }
        }
        if (y == 1) {
            if (pieces[x][y + 1] == empty.code() && pieces[x][y + 2] == empty.code()) {
                subpositons.add(new Position(this, x, y, x, y + 2));
            }
        }
    }

    void findBlackPawnMoves(int x, int y) {
        if (squareExists(x, y - 1)) {
            if (pieces[x][y - 1] == empty.code()) {
                subpositons.add(new Position(this, x, y, x, y - 1));
            }
        }

        if (squareExists(x + 1, y - 1)) {
            if (pieces[x + 1][y - 1] != empty.code()
                    && pieceIsWhite(pieces[x + 1][y - 1]) != whiteToMove) {
                subpositons.add(new Position(this, x, y, x + 1, y - 1));
            }
        }
        if (squareExists(x - 1, y - 1)) {
            if (pieces[x - 1][y - 1] != empty.code()
                    && pieceIsWhite(pieces[x - 1][y - 1]) != whiteToMove) {
                subpositons.add(new Position(this, x, y, x - 1, y - 1));
            }
        }
    }

    boolean kingIsInCheck(boolean white) {
        int x = 0, y = 0;
        if (white) {
            // Locate the white king...
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (pieces[i][j] == wKing.code()) {
                        x = i;
                        y = j;
                        break;
                    }
                }
            }
        } else {
            // Locate the black king...
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (pieces[i][j] == bKing.code()) {
                        x = i;
                        y = j;
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (pieces[i][j] != empty.code() && pieceIsWhite(pieces[i][j]) != white) {
                    if (rules.isMoveLegal(this, new Point(i, j), new Point(x, y),
                            true))
                        return true;
                }
            }
        }
        return false;
    }

    boolean squareExists(int x, int y) {
        return x >= 0 && y >= 0 && x < 8 && y < 8;
    }

    public void shiftDepth() {
        for (int i = 0; i < subpositons.size(); i++) {
            subpositons.get(i).shiftDepth();
        }
        depth--;
        if (depth < 0) {
            System.err.println("Creating a position with a depth less than 0");
        }
    }

    private boolean pieceIsWhite(byte piece) {
        return piece > 0;
    }

    private boolean isLandable(byte piece) {
        if (piece == empty.code()) {
            return true;
        } else {
            return pieceIsWhite(piece) != whiteToMove;
        }
    }

    public String toString() {
        String piecesS = "";
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                piecesS += pieces[i][j];
            }

        }
        return piecesS;
    }

    public static synchronized void addToTotal(){
        totalPositions ++;
    }

}