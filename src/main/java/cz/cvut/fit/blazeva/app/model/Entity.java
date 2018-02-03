package cz.cvut.fit.blazeva.app.model;

public class Entity {

    private EntityType type;

    public Entity(EntityType type) {
        this.type = type;
    }

    public EntityType getType() {
        return type;
    }
}
