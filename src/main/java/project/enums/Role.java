package project.enums;

public enum Role {
    ADMINISTRATOR(1),
    CLIENT(2),
    COACH(3);

    private final int id;

    Role(int id) {
        this.id = id;
    }
}
