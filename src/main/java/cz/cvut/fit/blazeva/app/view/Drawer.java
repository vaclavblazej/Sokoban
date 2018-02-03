package cz.cvut.fit.blazeva.app.view;

import cz.cvut.fit.blazeva.app.model.Asteroid;
import cz.cvut.fit.blazeva.app.model.EntityType;
import cz.cvut.fit.blazeva.app.model.Ship;
import cz.cvut.fit.blazeva.app.model.SpaceCamera;
import cz.cvut.fit.blazeva.util.WavefrontMeshLoader;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;

import static cz.cvut.fit.blazeva.app.control.Model.height;
import static cz.cvut.fit.blazeva.app.control.Model.width;
import static cz.cvut.fit.blazeva.app.model.EntityType.PARTICLE;
import static cz.cvut.fit.blazeva.app.model.EntityType.SHIP;
import static cz.cvut.fit.blazeva.app.model.EntityType.SHOT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Drawer {


    private Matrix4f projMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f viewProjMatrix = new Matrix4f();
    private Matrix4f invViewMatrix = new Matrix4f();
    private Matrix4f invViewProjMatrix = new Matrix4f();
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private SpaceCamera cam = new SpaceCamera();

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
    private static float maxLinearVel = 200.0f;

    private WavefrontMeshLoader.Mesh sphere;
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

    public void update(float dt, Program program){
        cam.update(dt);

        projMatrix.setPerspective((float) Math.toRadians(40.0f), (float) width / height, 0.1f, 5000.0f);
        viewMatrix.set(cam.rotation).invert(invViewMatrix);
        viewProjMatrix.set(projMatrix).mul(viewMatrix).invert(invViewProjMatrix);
        frustumIntersection.set(viewProjMatrix);

        /* Update the ship shader */
        glUseProgram(program.program(SHIP));
        glUniformMatrix4fv(program.viewUniform(SHIP), false, viewMatrix.get(matrixBuffer));
        glUniformMatrix4fv(program.projection(SHIP), false, projMatrix.get(matrixBuffer));

        /* Update the shot shader */
        glUseProgram(program.program(SHOT));
        glUniformMatrix4fv(program.projection(SHOT), false, matrixBuffer);

        /* Update the particle shader */
        glUseProgram(program.program(PARTICLE));
        glUniformMatrix4fv(program.projection(PARTICLE), false, matrixBuffer);
    }

    public void createEntities() throws IOException {
        createAsteroids();
        createShip();
        createSphere();
    }

    private void createSphere() throws IOException {
        WavefrontMeshLoader loader = new WavefrontMeshLoader();
        sphere = loader.loadMesh("sphere");
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

    public void draw(Program program) {
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        drawAsteroids(program, matrixBuffer);
        drawShips(program, matrixBuffer);
        drawVelocityCompass( matrixBuffer);
    }

    private void drawAsteroids(Program program, FloatBuffer matrixBuffer) {
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


    private void drawShips(Program program, FloatBuffer matrixBuffer) {
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

    private void drawVelocityCompass(FloatBuffer matrixBuffer) {
        glUseProgram(0);
        glEnable(GL_BLEND);
        glVertexPointer(3, GL_FLOAT, 0, sphere.positions);
        glEnableClientState(GL_NORMAL_ARRAY);
        glNormalPointer(GL_FLOAT, 0, sphere.normals);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadMatrixf(projMatrix.get(matrixBuffer));
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glTranslatef(0, -1, -4);
        glMultMatrixf(viewMatrix.get(matrixBuffer));
        glScalef(0.3f, 0.3f, 0.3f);
        glColor4f(0.1f, 0.1f, 0.1f, 0.2f);
        glDisable(GL_DEPTH_TEST);
        glDrawArrays(GL_TRIANGLES, 0, sphere.numVertices);
        glEnable(GL_DEPTH_TEST);
        glBegin(GL_LINES);
        glColor4f(1, 0, 0, 1);
        glVertex3f(0, 0, 0);
        glVertex3f(1, 0, 0);
        glColor4f(0, 1, 0, 1);
        glVertex3f(0, 0, 0);
        glVertex3f(0, 1, 0);
        glColor4f(0, 0, 1, 1);
        glVertex3f(0, 0, 0);
        glVertex3f(0, 0, 1);
        glColor4f(1, 1, 1, 1);
        glVertex3f(0, 0, 0);
        glVertex3f(cam.linearVel.x / maxLinearVel, cam.linearVel.y / maxLinearVel, cam.linearVel.z / maxLinearVel);
        glEnd();
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glDisableClientState(GL_NORMAL_ARRAY);
        glDisable(GL_BLEND);
    }
}
