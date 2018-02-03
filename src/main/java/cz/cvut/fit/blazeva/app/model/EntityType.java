package cz.cvut.fit.blazeva.app.model;

public enum EntityType {
    SHIP(0),
    ASTEROID(1),
    CUBEMAP(2),
    SHOT(3),
    PARTICLE(4),
    PLAYER(5);

    public static int numberOfEntityTypes = 7;

    public int id;

    EntityType(int id) {
        this.id = id;
    }
}
