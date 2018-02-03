package cz.cvut.fit.blazeva.app.model;

public class Player extends Entity {

    public int x = 1, y = 1;

    public Player() {
        super(EntityType.PLAYER);
    }
}
