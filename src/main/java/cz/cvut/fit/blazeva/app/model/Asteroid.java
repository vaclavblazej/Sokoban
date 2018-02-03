package cz.cvut.fit.blazeva.app.model;

public class Asteroid extends Entity{

    public double x, y, z;
    public float scale;

    public Asteroid() {
        super(EntityType.ASTEROID);
    }
}
