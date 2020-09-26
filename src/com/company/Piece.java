package com.company;

public enum Piece {
    bPawn((byte) -1), bRook((byte) -2), bKnight((byte) -3), bBishop((byte) -4), bQueen((byte) -5), bKing((byte) -6), wPawn((byte) 1), wRook((byte) 2), wKnight((byte) 3), wBishop((byte) 4), wQueen((byte) 5), wKing((byte) 6), empty((byte) 0);

    byte code;

    Piece(byte code) {
        this.code = code;
    }

    public byte code() {
        return code;
    }
}
