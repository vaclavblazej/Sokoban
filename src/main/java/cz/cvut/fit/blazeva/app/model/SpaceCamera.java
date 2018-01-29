package cz.cvut.fit.blazeva.app.model;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class SpaceCamera {
    public Vector3f linearAcc = new Vector3f();
    public Vector3f linearVel = new Vector3f();
    public float linearDamping = 0.08f;

    /**
     * ALWAYS rotation about the local XYZ axes of the camera!
     */
    public Vector3f angularAcc = new Vector3f();
    public Vector3f angularVel = new Vector3f();
    public float angularDamping = 0.5f;

    public Vector3d position = new Vector3d(0, 0, 10);
    public Quaternionf rotation = new Quaternionf();

    public SpaceCamera update(float dt) {
        // update linear velocity based on linear acceleration
        linearVel.fma(dt, linearAcc);
        // update angular velocity based on angular acceleration
        angularVel.fma(dt, angularAcc);
        // update the rotation based on the angular velocity
        rotation.integrate(dt, angularVel.x, angularVel.y, angularVel.z);
        angularVel.mul(1.0f - angularDamping * dt);
        // update position based on linear velocity
        position.fma(dt, linearVel);
        linearVel.mul(1.0f - linearDamping * dt);
        return this;
    }

    public Vector3f right(Vector3f dest) {
        return rotation.positiveX(dest);
    }

    public Vector3f up(Vector3f dest) {
        return rotation.positiveY(dest);
    }

    public Vector3f forward(Vector3f dest) {
        return rotation.positiveZ(dest).negate();
    }
}
