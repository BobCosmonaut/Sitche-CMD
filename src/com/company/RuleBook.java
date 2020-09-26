package com.company;

import java.awt.*;

import static com.company.Piece.*;

public class RuleBook {

    final Point whiteLong = new Point(2, 0);
    final Point whiteShort = new Point(6, 0);
    final Point blackLong = new Point(2, 7);
    final Point blackShort = new Point(6, 7);

    final Point whiteLongRook = new Point(0, 0);
    final Point whiteShortRook = new Point(7, 0);
    final Point blackShortRook = new Point(7, 7);
    final Point blackLongRook = new Point(0, 7);

    static final byte[][] initialPositionB = {
            {2, 3, 4, 5, 6, 4, 3, 2},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0,},
            {0, 0, 0, 0, 0, 0, 0, 0,},
            {0, 0, 0, 0, 0, 0, 0, 0,},
            {0, 0, 0, 0, 0, 0, 0, 0,},
            {-1, -1, -1, -1, -1, -1, -1, -1,},
            {-2, -3, -4, -5, -6, -4, -3, -2}};

    public RuleBook() {

    }

    public boolean isMoveLegal(Position position, Point original, Point newSquare, boolean hypothetical) {

       byte piece =  position.pieces[original.x][original.y];
        byte capturedPiece = position.pieces[newSquare.x][newSquare.y];

        // If captured piece is white and moving piece is white...
        if (pieceIsWhite(capturedPiece) && (pieceIsWhite(piece))) {
            return false;
        }
        // If captured piece is black and moving piece is black...
        if ((!pieceIsWhite(capturedPiece) && !squareIsEmpty(capturedPiece)) && !pieceIsWhite(piece)) {
            return false;
        }
        // if is Rook...
        if (piece == bRook.code() || piece == wRook.code()) {
            if (original.x == newSquare.x) {
                return !isObscuredVert(original, newSquare, position);

            } else if (original.y == newSquare.y) {
                return !isObscuredHoro(original, newSquare, position);
            } else {
                return false;
            }
        }
        if (piece == bPawn.code()) {
            if (squareIsEmpty(capturedPiece)) {
                if (original.y == newSquare.y + 1 && original.x == newSquare.x) {
                    if (newSquare.y == 0 && !hypothetical) {
                        position.pieces[newSquare.x][newSquare.y] = bQueen.code();
                    }
                    return true;
                } else if (original.y == 6 && squareIsEmpty(position.pieces[original.x][original.y - 1])
                        && squareIsEmpty(position.pieces[original.x][original.y - 2])
                        && original.x == newSquare.x && newSquare.y == 4) {
                    if (!hypothetical) {
                        position.enpassant.setLocation((byte)original.x, (byte)(original.y + 1));
                    }
                    return true;
                } else {
                    return false;
                }
            } else if (!squareIsEmpty(capturedPiece)) {
                if (original.y == newSquare.y + 1 && Math.abs(original.x - newSquare.x) == 1) {
                    return true;
                } else if (position.enpassant.x == (byte)(newSquare.x) && position.enpassant.y == (byte)(newSquare.y)) {
                    if (original.y == newSquare.y + 1 && Math.abs(original.x - newSquare.x) == 1) {
                        if (newSquare.y == 0 && !hypothetical) {
                            position.pieces[newSquare.x][newSquare.y] = bQueen.code;
                        }
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }

            } else {
                return false;
            }
        }

        if (piece == wPawn.code()) {
            if (squareIsEmpty(capturedPiece)) {
                if (original.y == newSquare.y - 1 && original.x == newSquare.x) {
                    if (newSquare.y == 7 && !hypothetical) {
                        position.pieces[newSquare.x][newSquare.y] = wQueen.code();
                    }
                    return true;
                } else if (original.y == 1 && squareIsEmpty(position.pieces[original.x][original.y + 1])
                        && squareIsEmpty(position.pieces[original.x][original.y + 2])
                        && original.x == newSquare.x && newSquare.y == 3) {
                    if (!hypothetical) {
                        position.enpassant.setLocation((byte)original.x, (byte)(original.y - 1));
                    }
                    return true;
                } else {
                    return false;
                }
            } else if (!squareIsEmpty(capturedPiece)) {
                if (original.y == newSquare.y - 1 && Math.abs(original.x - newSquare.x) == 1) {
                    if (newSquare.y == 7 && !hypothetical) {
                        position.pieces[newSquare.x][newSquare.y] = wQueen.code;
                    }
                    return true;
                } else {
                    return false;
                }
            } else if (original.y == 1 && squareIsEmpty(position.pieces[original.x][original.y + 1])) {
                if (original.y == newSquare.y - 1 && Math.abs(original.x - newSquare.x) == 1) {

                    return true;
                }
            } else {
                return false;
            }
        }

        if (piece == wKing.code() || piece == bKing.code()) {
            if ((Math.abs(newSquare.x - original.x) == 1 && Math.abs(newSquare.y - original.y) == 1)
                    || Math.abs(newSquare.x - original.x) + Math.abs(newSquare.y - original.y) == 1) {
                return true;
            } else if (piece == wKing.code()) {
                if (newSquare.equals(whiteLong) && position.whiteLong
                        && !isObscuredHoro(original, whiteLongRook, position)) {
                    if (!hypothetical) {
                        position.pieces[whiteLongRook.x][0] = empty.code();
                        position.pieces[whiteLong.x + 1][0] = wRook.code();
                    }
                    return true;

                } else if (newSquare.equals(whiteShort) && position.whiteShort
                        && !isObscuredHoro(original, whiteShortRook, position)) {
                    if (!hypothetical) {
                        position.pieces[whiteShortRook.x][0] = empty.code();
                        position.pieces[whiteShort.x - 1][0] = wRook.code();
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                if (newSquare.equals(blackLong) && position.blackLong
                        && !isObscuredHoro(original, blackLongRook, position)) {
                    if (!hypothetical) {
                        position.pieces[blackLongRook.x][7] = empty.code();
                        position.pieces[blackLong.x + 1][7] = bRook.code();
                    }
                    return true;
                } else if (newSquare.equals(blackShort) && position.blackShort
                        && !isObscuredHoro(original, blackShortRook, position)) {
                    if (!hypothetical) {
                        position.pieces[blackShortRook.x][7] = empty.code();
                        position.pieces[blackShort.x - 1][7] = bRook.code();
                    }
                    return true;
                }
                return false;
            }
        }

        if (piece == wKnight.code() || piece == bKnight.code()) {
            int dX = newSquare.x - original.x;
            int dY = newSquare.y - original.y;
            if ((Math.abs(dX) + Math.abs(dY)) == 3 && dX * dY != 0) {
                return true;
            } else {
                return false;
            }
        }

        if (piece == wBishop.code() || piece == bBishop.code()) {
            if (Math.abs(original.x - newSquare.x) == Math.abs(original.y - newSquare.y)) {
                if ((original.y - newSquare.y) / (original.x - newSquare.x) > 0) {
                    return !isObscuredRightDiag(original, newSquare, position);
                } else {
                    return !isObscuredLeftDiag(original, newSquare, position);
                }
            } else {
                return false;
            }
        }
        if (piece == wQueen.code() || piece == bQueen.code()) {
            if (Math.abs(original.x - newSquare.x) == Math.abs(original.y - newSquare.y)) {
                if ((original.y - newSquare.y) / (original.x - newSquare.x) > 0) {
                    return !isObscuredRightDiag(original, newSquare, position);
                } else {
                    return !isObscuredLeftDiag(original, newSquare, position);
                }
            } else if (original.x == newSquare.x) {
                if (!isObscuredVert(original, newSquare, position)) {
                    return true;
                } else {
                    return false;
                }
            } else if (original.y == newSquare.y) {
                if (!isObscuredHoro(original, newSquare, position)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        System.err.println("Returning false at the end of ruleBook " + piece);
        return false;
    }

    private boolean isObscuredHoro(Point original, Point newPoint, Position position) {
        if (original.x > newPoint.x) {
            int i = original.x - 1;

            while (i > newPoint.x) {
                if (position.pieces[i][original.y] != empty.code()) {
                    return true;
                }
                i--;
            }
            return false;
        } else {
            int i = original.x + 1;

            while (i < newPoint.x) {
                if (position.pieces[i][original.y] != empty.code()) {
                    return true;
                }
                i++;
            }
            return false;
        }
    }

    private boolean isObscuredVert(Point original, Point newPoint, Position position) {
        if (original.y > newPoint.y) {
            int i = original.y - 1;
            while (i > newPoint.y) {
                if (position.pieces[original.x][i] != empty.code()) {
                    return true;
                }
                i--;
            }
            return false;
        } else {
            int i = original.y + 1;
            while (i < newPoint.y) {
                if (position.pieces[original.x][i] != empty.code()) {
                    return true;
                }
                i++;
            }
            return false;
        }
    }

    private boolean isObscuredRightDiag(Point original, Point newPoint, Position position) {
        if (original.y > newPoint.y) {
            int i = 1;
            while (i < original.y - newPoint.y) {
                if (position.pieces[newPoint.x + i][newPoint.y + i] != empty.code()) {
                    return true;
                }
                i++;
            }
            return false;
        } else {
            int i = 1;
            while (i < newPoint.y - original.y) {
                if (position.pieces[original.x + i][original.y + i] != empty.code()) {
                    return true;
                }
                i++;
            }
            return false;
        }
    }

    private boolean isObscuredLeftDiag(Point original, Point newPoint, Position position) {
        if (original.y > newPoint.y) {
            int i = 1;
            while (i < original.y - newPoint.y) {
                if (position.pieces[original.x + i][original.y - i] != empty.code()) {
                    return true;
                }
                i++;
            }
            return false;
        } else {
            int i = 1;
            while (i < newPoint.y - original.y) {
                if (position.pieces[original.x - i][original.y + i] != empty.code()) {
                    return true;
                }
                i++;
            }
            return false;
        }
    }

    private boolean pieceIsWhite(byte piece) {
        return piece > 0;
    }

    private boolean squareIsEmpty(byte square) {
        return square == 0;
    }
}
