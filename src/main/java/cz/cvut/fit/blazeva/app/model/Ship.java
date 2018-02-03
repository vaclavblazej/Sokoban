package cz.cvut.fit.blazeva.app.model;

public class Ship extends Entity {

    public double x, y, z;
    public long lastShotTime;

    public Ship() {
        super(EntityType.SHIP);
    }
}
