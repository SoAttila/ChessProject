package model;

public enum PieceType {
    PAWN(""), KNIGHT("N"), BISHOP("B"), ROOK("R"), QUEEN("Q"), KING("K");
    private final String abbreviation;

    PieceType(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @Override
    public String toString() {
        return abbreviation;
    }
}
