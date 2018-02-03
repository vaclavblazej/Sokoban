package cz.cvut.fit.blazeva.app.view;

import cz.cvut.fit.blazeva.app.model.Asteroid;
import cz.cvut.fit.blazeva.app.model.EntityType;
import cz.cvut.fit.blazeva.app.model.Ship;
import cz.cvut.fit.blazeva.app.model.SpaceCamera;
import cz.cvut.fit.blazeva.util.WavefrontMeshLoader;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.FloatBuffer;

import static cz.cvut.fit.blazeva.app.model.EntityType.SHIP;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Drawer {


    private int asteroidCount = 512;
    private int asteroidPositionVbo;
    private int asteroidNormalVbo;
    private float maxAsteroidRadius = 20.0f;
    private WavefrontMeshLoader.Mesh asteroid;
    private FrustumIntersection frustumIntersection = new FrustumIntersection();
    private static float shipSpread = 1000.0f;

    private Asteroid[] asteroids = new Asteroid[asteroidCount];

    {
        for (int i = 0; i < asteroids.length; i++) {
            Asteroid asteroid = new Asteroid();
            float scale = (float) ((Math.random() * 0.5 + 0.5) * maxAsteroidRadius);
            asteroid.x = (Math.random() - 0.5) * shipSpread;
            asteroid.y = (Math.random() - 0.5) * shipSpread;
            asteroid.z = (Math.random() - 0.5) * shipSpread;
            asteroid.scale = scale;
            asteroids[i] = asteroid;
        }
    }

    private Matrix4f modelMatrix = new Matrix4f();


    private WavefrontMeshLoader.Mesh ship;
    private static float shipRadius = 4.0f;
    private int shipPositionVbo;
    private int shipNormalVbo;
    private int shipCount = 128;
    private Ship[] ships = new Ship[shipCount];

    {
        for (int i = 0; i < ships.length; i++) {
            Ship ship = new Ship();
            ship.x = (Math.random() - 0.5) * shipSpread;
            ship.y = (Math.random() - 0.5) * shipSpread;
            ship.z = (Math.random() - 0.5) * shipSpread;
            ships[i] = ship;
        }
    }

    public void createEntities() throws IOException {
        createAsteroids();
        createShip();
    }

    private void createAsteroids() throws IOException {
        WavefrontMeshLoader loader = new WavefrontMeshLoader();
        asteroid = loader.loadMesh("asteroid");
        asteroidPositionVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, asteroidPositionVbo);
        glBufferData(GL_ARRAY_BUFFER, asteroid.positions, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        asteroidNormalVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, asteroidNormalVbo);
        glBufferData(GL_ARRAY_BUFFER, asteroid.normals, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void createShip() throws IOException {
        WavefrontMeshLoader loader = new WavefrontMeshLoader();
        ship = loader.loadMesh("ship");
        shipPositionVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, shipPositionVbo);
        glBufferData(GL_ARRAY_BUFFER, ship.positions, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        shipNormalVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, shipNormalVbo);
        glBufferData(GL_ARRAY_BUFFER, ship.normals, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public Ship getShip(int idx){
        return ships[idx];
    }

    public void draw(Program program,SpaceCamera cam, FloatBuffer matrixBuffer) {
        drawAsteroids(program, cam, matrixBuffer);
        drawShips(program, cam, matrixBuffer);
    }

    private void drawAsteroids(Program program, SpaceCamera cam, FloatBuffer matrixBuffer) {
        glUseProgram(program.program(EntityType.ASTEROID));
        glBindBuffer(GL_ARRAY_BUFFER, asteroidPositionVbo);
        glVertexPointer(3, GL_FLOAT, 0, 0);
        glEnableClientState(GL_NORMAL_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, asteroidNormalVbo);
        glNormalPointer(GL_FLOAT, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (Asteroid asteroid : asteroids) {
            if (asteroid == null)
                continue;
            float x = (float) (asteroid.x - cam.position.x);
            float y = (float) (asteroid.y - cam.position.y);
            float z = (float) (asteroid.z - cam.position.z);
            if (frustumIntersection.testSphere(x, y, z, asteroid.scale)) {
                modelMatrix.translation(x, y, z);
                modelMatrix.scale(asteroid.scale);
                glUniformMatrix4fv(program.modelUniform(SHIP), false, modelMatrix.get(matrixBuffer));
                glDrawArrays(GL_TRIANGLES, 0, this.asteroid.numVertices);
            }
        }
        glDisableClientState(GL_NORMAL_ARRAY);
    }


    private void drawShips(Program program, SpaceCamera cam, FloatBuffer matrixBuffer) {
        glUseProgram(program.program(SHIP));
        glBindBuffer(GL_ARRAY_BUFFER, shipPositionVbo);
        glVertexPointer(3, GL_FLOAT, 0, 0);
        glEnableClientState(GL_NORMAL_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, shipNormalVbo);
        glNormalPointer(GL_FLOAT, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (Ship ship : ships) {
            if (ship == null)
                continue;
            float x = (float) (ship.x - cam.position.x);
            float y = (float) (ship.y - cam.position.y);
            float z = (float) (ship.z - cam.position.z);
            if (frustumIntersection.testSphere(x, y, z, shipRadius)) {
                modelMatrix.translation(x, y, z);
                modelMatrix.scale(shipRadius);
                glUniformMatrix4fv(program.modelUniform(SHIP), false, modelMatrix.get(matrixBuffer));
                glDrawArrays(GL_TRIANGLES, 0, this.ship.numVertices);
            }
        }
        glDisableClientState(GL_NORMAL_ARRAY);
    }
}
