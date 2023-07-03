package model;

public enum PlayerEnum {
    WHITE("W"), BLACK("B");
    private final String abbreviation;

    PlayerEnum(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @Override
    public String toString() {
        return abbreviation;
    }
}
