package model;

import java.util.Objects;

public class Piece {
    private final PieceType type;
    private final PlayerEnum color;
    private boolean hasMoved;

    public Piece(PieceType type, PlayerEnum color) {
        this.type = type;
        this.color = color;
        hasMoved = false;
    }

    public PieceType getType() {
        return type;
    }

    public PlayerEnum getColor() {
        return color;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return type == piece.type && color == piece.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, color, hasMoved);
    }

    @Override
    public String toString(){
        switch (type) {
            case PAWN -> {
                return "";
            }
            case KNIGHT -> {
                return "N";
            }
            case BISHOP -> {
                return "B";
            }
            case ROOK -> {
                return "R";
            }
            case QUEEN -> {
                return "Q";
            }
            case KING -> {
                return "K";
            }
        }
        return "";
    }
}
