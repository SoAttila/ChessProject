package model;

public record Move(Position from, Position to) {

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }
}
