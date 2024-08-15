package model;

import java.util.Objects;

public class Position {
    private final int file;
    private final int rank;

    public Position(int file, int rank) {
        this.file = file;
        this.rank = rank;
    }

    public Position(Position other) {
        this.file = other.file;
        this.rank = other.rank;
    }

    public Position(String notation) {
        file = notation.charAt(0) - 'a';
        rank = ModelConstants.BOARD_SIZE - Character.getNumericValue(notation.charAt(1));
    }

    public static char getChar(int i) {
        return i < 0 || i > 25 ? '?' : (char) ('a' + i);
    }

    public int getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return file == position.file && rank == position.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, rank);
    }

    @Override
    public String toString() {
        return getChar(file) + "" + (ModelConstants.BOARD_SIZE - rank);
    }
}
