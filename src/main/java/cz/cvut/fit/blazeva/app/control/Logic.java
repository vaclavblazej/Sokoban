package cz.cvut.fit.blazeva.app.control;

import cz.cvut.fit.blazeva.app.view.Drawer;
import cz.cvut.fit.blazeva.app.view.Program;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static cz.cvut.fit.blazeva.app.control.Model.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Logic {

    private static float shotVelocity = 450.0f;
    private static float shotSeparation = 0.8f;
    private static int shotMilliseconds = 80;
    private static int shotOpponentMilliseconds = 200;
    //    private static float straveThrusterAccFactor = 20.0f;
//    private static float mainThrusterAccFactor = 50.0f;
    private static float maxShotLifetime = 30.0f;
    //    private static float maxParticleLifetime = 1.0f;
//    private static float shotSize = 0.5f;
//    private static float particleSize = 1.0f;
//    private static final int explosionParticles = 60;
    private static final int maxParticles = 4096;
//    private static final int maxShots = 1024;

    private ByteBuffer quadVertices;


    private Vector3d[] projectilePositions = new Vector3d[1024];
    private Vector4f[] projectileVelocities = new Vector4f[1024];

    {
        for (int i = 0; i < projectilePositions.length; i++) {
            Vector3d projectilePosition = new Vector3d(0, 0, 0);
            projectilePositions[i] = projectilePosition;
            Vector4f projectileVelocity = new Vector4f(0, 0, 0, 0);
            projectileVelocities[i] = projectileVelocity;
        }
    }

    private Vector3d[] particlePositions = new Vector3d[maxParticles];
    private Vector4d[] particleVelocities = new Vector4d[maxParticles];

    {
        for (int i = 0; i < particlePositions.length; i++) {
            Vector3d particlePosition = new Vector3d(0, 0, 0);
            particlePositions[i] = particlePosition;
            Vector4d particleVelocity = new Vector4d(0, 0, 0, 0);
            particleVelocities[i] = particleVelocity;
        }
    }

    //    private FloatBuffer shotsVertices = BufferUtils.createFloatBuffer(6 * 6 * maxShots);
//    private FloatBuffer particleVertices = BufferUtils.createFloatBuffer(6 * 6 * maxParticles);
    private FloatBuffer crosshairVertices = BufferUtils.createFloatBuffer(6 * 2);

//    private ByteBuffer charBuffer = BufferUtils.createByteBuffer(16 * 270);

    private long lastShotTime = 0L;
    private int shootingShip = 0;
    private long lastTime = System.nanoTime();
    private Vector3d tmp = new Vector3d();
    private Vector3d newPosition = new Vector3d();
    private Vector3f tmp2 = new Vector3f();
    private Vector3f tmp3 = new Vector3f();
    private Vector3f tmp4 = new Vector3f();
    private FrustumIntersection frustumIntersection = new FrustumIntersection();
    private static float shipRadius = 4.0f;

    private Program program = new Program();
    private Drawer drawer = new Drawer();


    private void createFullScreenQuad() {
        quadVertices = BufferUtils.createByteBuffer(4 * 2 * 6);
        FloatBuffer fv = quadVertices.asFloatBuffer();
        fv.put(-1.0f).put(-1.0f);
        fv.put(1.0f).put(-1.0f);
        fv.put(1.0f).put(1.0f);
        fv.put(1.0f).put(1.0f);
        fv.put(-1.0f).put(1.0f);
        fv.put(-1.0f).put(-1.0f);
    }


//    private void createCubemapTexture() throws IOException {
//        int tex = glGenTextures();
//        glBindTexture(GL_TEXTURE_CUBE_MAP, tex);
//        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//        ByteBuffer imageBuffer;
//        IntBuffer w = BufferUtils.createIntBuffer(1);
//        IntBuffer h = BufferUtils.createIntBuffer(1);
//        IntBuffer comp = BufferUtils.createIntBuffer(1);
//        String[] names = {"right", "left", "top", "bottom", "front", "back"};
//        ByteBuffer image;
//        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
//        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_GENERATE_MIPMAP, GL_TRUE);
//        for (int i = 0; i < 6; i++) {
//            final String resource = "cz/cvut/fit/blazeva/background/space_" + names[i] + (i + 1) + ".jpg";
//            imageBuffer = ioResourceToByteBuffer(resource, 8 * 1024);
//            if (!stbi_info_from_memory(imageBuffer, w, h, comp))
//                throw new IOException("Failed to read image information: " + stbi_failure_reason());
//            image = stbi_load_from_memory(imageBuffer, w, h, comp, 0);
//            if (image == null)
//                throw new IOException("Failed to load image: " + stbi_failure_reason());
//            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB8, w.get(0), h.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, image);
//            stbi_image_free(image);
//        }
//        if (caps.OpenGL32 || caps.GL_ARB_seamless_cube_map) {
//            glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
//        }
//    }

    private void update() {
        long thisTime = System.nanoTime();
        float dt = (thisTime - lastTime) / 1E9f;
        lastTime = thisTime;
        updateShots(dt);
//        updateParticles(dt);

        drawer.update(dt, program);

        /* Update the background shader */
//        glUseProgram(program.program(CUBEMAP));
//        glUniformMatrix4fv(program.invViewProjection(CUBEMAP), false, invViewProjMatrix.get(matrixBuffer));

        updateControls();

        /* Let the player shoot a bullet */
        if (leftMouseDown && (thisTime - lastShotTime >= 1E6 * shotMilliseconds)) {
//            shoot();
            lastShotTime = thisTime;
        }
        /* Let the opponent shoot a bullet */
//        shootFromShip(thisTime, shootingShip);
    }

    private boolean keyTapped(int keycode) {
        final boolean b = Model.keyTapped[keycode];
        Model.keyTapped[keycode] = false;
        return b;
    }

    private void updateControls() {
//        cam.linearAcc.zero();
        float rotZ = 0.0f;
//        if (keyDown[GLFW_KEY_W]) cam.linearAcc.fma(mainThrusterAccFactor, cam.forward(tmp2));
//        if (keyDown[GLFW_KEY_S]) cam.linearAcc.fma(-mainThrusterAccFactor, cam.forward(tmp2));
//        if (keyDown[GLFW_KEY_D]) cam.linearAcc.fma(straveThrusterAccFactor, cam.right(tmp2));
//        if (keyDown[GLFW_KEY_A]) cam.linearAcc.fma(-straveThrusterAccFactor, cam.right(tmp2));
        if (keyTapped(GLFW_KEY_LEFT)) Model.scenario.move(-1, 0);
        if (keyTapped(GLFW_KEY_RIGHT)) Model.scenario.move(1, 0);
        if (keyTapped(GLFW_KEY_UP)) Model.scenario.move(0, 1);
        if (keyTapped(GLFW_KEY_DOWN)) Model.scenario.move(0, -1);
        if (keyTapped(GLFW_KEY_R)) {
            Model.level--;
            Model.loadNextScenario();
        }
//        if (keyDown[GLFW_KEY_SPACE]) cam.linearAcc.fma(straveThrusterAccFactor, cam.up(tmp2));
//        if (keyDown[GLFW_KEY_LEFT_CONTROL]) cam.linearAcc.fma(-straveThrusterAccFactor, cam.up(tmp2));
        if (rightMouseDown) {
            final float xxx = 2.0f * mouseX * mouseX * mouseX;
            final float yyy = 2.0f * mouseY * mouseY * mouseY;
//            cam.angularAcc.set(yyy, xxx, rotZ);
//        } else if (!rightMouseDown) {
//            cam.angularAcc.set(0, 0, rotZ);
        }
//        double linearVelAbs = cam.linearVel.length();
//        if (linearVelAbs > maxLinearVel) {
//            cam.linearVel.normalize().mul(maxLinearVel);
//        }
    }

    /* Create all needed GL resources */
    public void initialize() throws IOException {
//        createCubemapTexture();
//        createFullScreenQuad();
//        program.initializePrograms();
        drawer.createEntities();
    }

//    private static Vector3f intercept(Vector3d shotOrigin, float shotSpeed, Vector3d targetOrigin, Vector3f targetVel, Vector3f out) {
//        float dirToTargetX = (float) (targetOrigin.x - shotOrigin.x);
//        float dirToTargetY = (float) (targetOrigin.y - shotOrigin.y);
//        float dirToTargetZ = (float) (targetOrigin.z - shotOrigin.z);
//        float len = (float) Math.sqrt(dirToTargetX * dirToTargetX + dirToTargetY * dirToTargetY + dirToTargetZ * dirToTargetZ);
//        dirToTargetX /= len;
//        dirToTargetY /= len;
//        dirToTargetZ /= len;
//        float targetVelOrthDot = targetVel.x * dirToTargetX + targetVel.y * dirToTargetY + targetVel.z * dirToTargetZ;
//        float targetVelOrthX = dirToTargetX * targetVelOrthDot;
//        float targetVelOrthY = dirToTargetY * targetVelOrthDot;
//        float targetVelOrthZ = dirToTargetZ * targetVelOrthDot;
//        float targetVelTangX = targetVel.x - targetVelOrthX;
//        float targetVelTangY = targetVel.y - targetVelOrthY;
//        float targetVelTangZ = targetVel.z - targetVelOrthZ;
//        float shotVelSpeed = (float) Math.sqrt(targetVelTangX * targetVelTangX + targetVelTangY * targetVelTangY + targetVelTangZ * targetVelTangZ);
//        if (shotVelSpeed > shotSpeed) {
//            return null;
//        }
//        float shotSpeedOrth = (float) Math.sqrt(shotSpeed * shotSpeed - shotVelSpeed * shotVelSpeed);
//        float shotVelOrthX = dirToTargetX * shotSpeedOrth;
//        float shotVelOrthY = dirToTargetY * shotSpeedOrth;
//        float shotVelOrthZ = dirToTargetZ * shotSpeedOrth;
//        return out.set(shotVelOrthX + targetVelTangX, shotVelOrthY + targetVelTangY, shotVelOrthZ + targetVelTangZ).normalize();
//    }

//    private void shootFromShip(long thisTime, int index) {
//        Ship ship = drawer.getShip(index);
//        if (ship == null) {
//            return;
//        }
//        if (thisTime - ship.lastShotTime < 1E6 * shotOpponentMilliseconds) {
//            return;
//        }
//        ship.lastShotTime = thisTime;
//        Vector3d shotPos = tmp.set(ship.x, ship.y, ship.z).sub(cam.position).negate().normalize().mul(1.01f * shipRadius).add(ship.x, ship.y, ship.z);
//        Vector3f icept = intercept(shotPos, shotVelocity, cam.position, cam.linearVel, tmp2);
//        if (icept == null) {
//            return;
//        }
//        // jitter the direction a bit
//        GeometryUtils.perpendicular(icept, tmp3, tmp4);
//        icept.fma(((float) Math.random() * 2.0f - 1.0f) * 0.01f, tmp3);
//        icept.fma(((float) Math.random() * 2.0f - 1.0f) * 0.01f, tmp4);
//        icept.normalize();
//        for (int i = 0; i < projectilePositions.length; i++) {
//            Vector3d projectilePosition = projectilePositions[i];
//            Vector4f projectileVelocity = projectileVelocities[i];
//            if (projectileVelocity.w <= 0.0f) {
//                projectilePosition.set(shotPos);
//                projectileVelocity.x = tmp2.x * shotVelocity;
//                projectileVelocity.y = tmp2.y * shotVelocity;
//                projectileVelocity.z = tmp2.z * shotVelocity;
//                projectileVelocity.w = 0.01f;
//                break;
//            }
//        }
//    }

//    private void shoot() {
//        boolean firstShot = false;
//        for (int i = 0; i < projectilePositions.length; i++) {
//            Vector3d projectilePosition = projectilePositions[i];
//            Vector4f projectileVelocity = projectileVelocities[i];
//            invViewProjMatrix.transformProject(tmp2.set(mouseX, -mouseY, 1.0f)).normalize();
//            if (projectileVelocity.w <= 0.0f) {
//                projectileVelocity.x = cam.linearVel.x + tmp2.x * shotVelocity;
//                projectileVelocity.y = cam.linearVel.y + tmp2.y * shotVelocity;
//                projectileVelocity.z = cam.linearVel.z + tmp2.z * shotVelocity;
//                projectileVelocity.w = 0.01f;
//                if (!firstShot) {
//                    projectilePosition.set(cam.right(tmp3)).mul(shotSeparation).add(cam.position);
//                    firstShot = true;
//                } else {
//                    projectilePosition.set(cam.right(tmp3)).mul(-shotSeparation).add(cam.position);
//                    break;
//                }
//            }
//        }
//    }

//    private void drawCubemap() {
//        glUseProgram(program.program(CUBEMAP));
//        glVertexPointer(2, GL_FLOAT, 0, quadVertices);
//        glDrawArrays(GL_TRIANGLES, 0, 6);
//    }


//    private void drawParticles() {
//        particleVertices.clear();
//        int num = 0;
//        for (int i = 0; i < particlePositions.length; i++) {
//            Vector3d particlePosition = particlePositions[i];
//            Vector4d particleVelocity = particleVelocities[i];
//            if (particleVelocity.w > 0.0f) {
//                float x = (float) (particlePosition.x - cam.position.x);
//                float y = (float) (particlePosition.y - cam.position.y);
//                float z = (float) (particlePosition.z - cam.position.z);
//                if (frustumIntersection.testPoint(x, y, z)) {
//                    float w = (float) particleVelocity.w;
//                    viewMatrix.transformPosition(tmp2.set(x, y, z));
//                    particleVertices.put(tmp2.x - particleSize).put(tmp2.y - particleSize).put(tmp2.z).put(w).put(-1).put(-1);
//                    particleVertices.put(tmp2.x + particleSize).put(tmp2.y - particleSize).put(tmp2.z).put(w).put(1).put(-1);
//                    particleVertices.put(tmp2.x + particleSize).put(tmp2.y + particleSize).put(tmp2.z).put(w).put(1).put(1);
//                    particleVertices.put(tmp2.x + particleSize).put(tmp2.y + particleSize).put(tmp2.z).put(w).put(1).put(1);
//                    particleVertices.put(tmp2.x - particleSize).put(tmp2.y + particleSize).put(tmp2.z).put(w).put(-1).put(1);
//                    particleVertices.put(tmp2.x - particleSize).put(tmp2.y - particleSize).put(tmp2.z).put(w).put(-1).put(-1);
//                    num++;
//                }
//            }
//        }
//        particleVertices.flip();
//        if (num > 0) {
//            glUseProgram(program.program(PARTICLE));
//            glDepthMask(false);
//            glEnable(GL_BLEND);
//            glVertexPointer(4, GL_FLOAT, 6 * 4, particleVertices);
//            particleVertices.position(4);
//            glTexCoordPointer(2, GL_FLOAT, 6 * 4, particleVertices);
//            particleVertices.position(0);
//            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//            glDrawArrays(GL_TRIANGLES, 0, num * 6);
//            glDisableClientState(GL_TEXTURE_COORD_ARRAY);
//            glDisable(GL_BLEND);
//            glDepthMask(true);
//        }
//    }

//    private void drawShots() {
//        shotsVertices.clear();
//        int num = 0;
//        for (int i = 0; i < projectilePositions.length; i++) {
//            Vector3d projectilePosition = projectilePositions[i];
//            Vector4f projectileVelocity = projectileVelocities[i];
//            if (projectileVelocity.w > 0.0f) {
//                float x = (float) (projectilePosition.x - cam.position.x);
//                float y = (float) (projectilePosition.y - cam.position.y);
//                float z = (float) (projectilePosition.z - cam.position.z);
//                if (frustumIntersection.testPoint(x, y, z)) {
//                    float w = projectileVelocity.w;
//                    viewMatrix.transformPosition(tmp2.set(x, y, z));
//                    shotsVertices.put(tmp2.x - shotSize).put(tmp2.y - shotSize).put(tmp2.z).put(w).put(-1).put(-1);
//                    shotsVertices.put(tmp2.x + shotSize).put(tmp2.y - shotSize).put(tmp2.z).put(w).put(1).put(-1);
//                    shotsVertices.put(tmp2.x + shotSize).put(tmp2.y + shotSize).put(tmp2.z).put(w).put(1).put(1);
//                    shotsVertices.put(tmp2.x + shotSize).put(tmp2.y + shotSize).put(tmp2.z).put(w).put(1).put(1);
//                    shotsVertices.put(tmp2.x - shotSize).put(tmp2.y + shotSize).put(tmp2.z).put(w).put(-1).put(1);
//                    shotsVertices.put(tmp2.x - shotSize).put(tmp2.y - shotSize).put(tmp2.z).put(w).put(-1).put(-1);
//                    num++;
//                }
//            }
//        }
//        shotsVertices.flip();
//        if (num > 0) {
//            glUseProgram(program.program(SHOT));
//            glDepthMask(false);
//            glEnable(GL_BLEND);
//            glVertexPointer(4, GL_FLOAT, 6 * 4, shotsVertices);
//            shotsVertices.position(4);
//            glTexCoordPointer(2, GL_FLOAT, 6 * 4, shotsVertices);
//            shotsVertices.position(0);
//            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//            glDrawArrays(GL_TRIANGLES, 0, num * 6);
//            glDisableClientState(GL_TEXTURE_COORD_ARRAY);
//            glDisable(GL_BLEND);
//            glDepthMask(true);
//        }
//    }

//    private void drawVelocityCompass() {
//        glUseProgram(0);
//        glEnable(GL_BLEND);
//        glVertexPointer(3, GL_FLOAT, 0, sphere.positions);
//        glEnableClientState(GL_NORMAL_ARRAY);
//        glNormalPointer(GL_FLOAT, 0, sphere.normals);
//        glMatrixMode(GL_PROJECTION);
//        glPushMatrix();
//        glLoadMatrixf(projMatrix.get(matrixBuffer));
//        glMatrixMode(GL_MODELVIEW);
//        glPushMatrix();
//        glLoadIdentity();
//        glTranslatef(0, -1, -4);
//        glMultMatrixf(viewMatrix.get(matrixBuffer));
//        glScalef(0.3f, 0.3f, 0.3f);
//        glColor4f(0.1f, 0.1f, 0.1f, 0.2f);
//        glDisable(GL_DEPTH_TEST);
//        glDrawArrays(GL_TRIANGLES, 0, sphere.numVertices);
//        glEnable(GL_DEPTH_TEST);
//        glBegin(GL_LINES);
//        glColor4f(1, 0, 0, 1);
//        glVertex3f(0, 0, 0);
//        glVertex3f(1, 0, 0);
//        glColor4f(0, 1, 0, 1);
//        glVertex3f(0, 0, 0);
//        glVertex3f(0, 1, 0);
//        glColor4f(0, 0, 1, 1);
//        glVertex3f(0, 0, 0);
//        glVertex3f(0, 0, 1);
//        glColor4f(1, 1, 1, 1);
//        glVertex3f(0, 0, 0);
//        glVertex3f(cam.linearVel.x / maxLinearVel, cam.linearVel.y / maxLinearVel, cam.linearVel.z / maxLinearVel);
//        glEnd();
//        glPopMatrix();
//        glMatrixMode(GL_PROJECTION);
//        glPopMatrix();
//        glMatrixMode(GL_MODELVIEW);
//        glDisableClientState(GL_NORMAL_ARRAY);
//        glDisable(GL_BLEND);
//    }

//    private void drawHudShotDirection() {
//        glUseProgram(0);
//        Ship enemyShip = drawer.getShip(shootingShip);
//        if (enemyShip == null)
//            return;
//        Vector3d targetOrigin = tmp;
//        targetOrigin.set(enemyShip.x, enemyShip.y, enemyShip.z);
//        Vector3f interceptorDir = intercept(cam.position, shotVelocity, targetOrigin, tmp3.set(cam.linearVel).negate(), tmp2);
//        viewMatrix.transformDirection(interceptorDir);
//        if (interceptorDir.z > 0.0)
//            return;
//        projMatrix.transformProject(interceptorDir);
//        float crosshairSize = 0.01f;
//        float xs = crosshairSize * height / width;
//        float ys = crosshairSize;
//        crosshairVertices.clear();
//        crosshairVertices.put(interceptorDir.x - xs).put(interceptorDir.y - ys);
//        crosshairVertices.put(interceptorDir.x + xs).put(interceptorDir.y - ys);
//        crosshairVertices.put(interceptorDir.x + xs).put(interceptorDir.y + ys);
//        crosshairVertices.put(interceptorDir.x - xs).put(interceptorDir.y + ys);
//        crosshairVertices.flip();
//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
//        glVertexPointer(2, GL_FLOAT, 0, crosshairVertices);
//        glDrawArrays(GL_QUADS, 0, 4);
//        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//    }

//    private void drawHudShip() {
//        glUseProgram(0);
//        Ship enemyShip = drawer.getShip(shootingShip);
//        if (enemyShip == null)
//            return;
//        Vector3f targetOrigin = tmp2;
//        targetOrigin.set((float) (enemyShip.x - cam.position.x),
//                (float) (enemyShip.y - cam.position.y),
//                (float) (enemyShip.z - cam.position.z));
//        tmp3.set(tmp2);
//        viewMatrix.transformPosition(targetOrigin);
//        boolean backward = targetOrigin.z > 0.0f;
//        if (backward)
//            return;
//        projMatrix.transformProject(targetOrigin);
//        if (targetOrigin.x < -1.0f) targetOrigin.x = -1.0f;
//        if (targetOrigin.x > 1.0f) targetOrigin.x = 1.0f;
//        if (targetOrigin.y < -1.0f) targetOrigin.y = -1.0f;
//        if (targetOrigin.y > 1.0f) targetOrigin.y = 1.0f;
//        float crosshairSize = 0.03f;
//        float xs = crosshairSize * height / width;
//        float ys = crosshairSize;
//        crosshairVertices.clear();
//        crosshairVertices.put(targetOrigin.x - xs).put(targetOrigin.y - ys);
//        crosshairVertices.put(targetOrigin.x + xs).put(targetOrigin.y - ys);
//        crosshairVertices.put(targetOrigin.x + xs).put(targetOrigin.y + ys);
//        crosshairVertices.put(targetOrigin.x - xs).put(targetOrigin.y + ys);
//        crosshairVertices.flip();
//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
//        glVertexPointer(2, GL_FLOAT, 0, crosshairVertices);
//        glDrawArrays(GL_QUADS, 0, 4);
//        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//        // Draw distance text of enemy
//        int quads = stb_easy_font_print(0, 0, Integer.toString((int) (tmp3.length())), null, charBuffer);
//        glVertexPointer(2, GL_FLOAT, 16, charBuffer);
//        glPushMatrix();
//        // Scroll
//        glTranslatef(targetOrigin.x, targetOrigin.y - crosshairSize * 1.1f, 0f);
//        float aspect = (float) width / height;
//        glScalef(1.0f / 500.0f, -1.0f / 500.0f * aspect, 0.0f);
//        glDrawArrays(GL_QUADS, 0, quads * 4);
//        glPopMatrix();
//    }

//    private boolean narrowphase(FloatBuffer data, double x, double y, double z, float scale, Vector3d pOld, Vector3d pNew, Vector3d intersectionPoint, Vector3f normal) {
//        tmp2.set(tmp.set(pOld).sub(x, y, z)).div(scale);
//        tmp3.set(tmp.set(pNew).sub(x, y, z)).div(scale);
//        data.clear();
//        boolean intersects = false;
//        while (data.hasRemaining() && !intersects) {
//            float v0X = data.get();
//            float v0Y = data.get();
//            float v0Z = data.get();
//            float v1X = data.get();
//            float v1Y = data.get();
//            float v1Z = data.get();
//            float v2X = data.get();
//            float v2Y = data.get();
//            float v2Z = data.get();
//            if (Intersectionf.intersectLineSegmentTriangle(tmp2.x, tmp2.y, tmp2.z, tmp3.x, tmp3.y, tmp3.z, v0X, v0Y, v0Z, v1X, v1Y, v1Z, v2X, v2Y, v2Z, 1E-6f, tmp2)) {
//                intersectionPoint.x = tmp2.x * scale + x;
//                intersectionPoint.y = tmp2.y * scale + y;
//                intersectionPoint.z = tmp2.z * scale + z;
//                GeometryUtils.normal(v0X, v0Y, v0Z, v1X, v1Y, v1Z, v2X, v2Y, v2Z, normal);
//                intersects = true;
//            }
//        }
//        data.clear();
//        return intersects;
//    }

//    private static boolean broadphase(double x, double y, double z, float boundingRadius, float scale, Vector3d pOld, Vector3d pNew) {
//        return Intersectiond.testLineSegmentSphere(pOld.x, pOld.y, pOld.z, pNew.x, pNew.y, pNew.z, x, y, z, boundingRadius * boundingRadius * scale * scale);
//    }

//    private void updateParticles(float dt) {
//        for (int i = 0; i < particlePositions.length; i++) {
//            Vector4d particleVelocity = particleVelocities[i];
//            if (particleVelocity.w <= 0.0f)
//                continue;
//            particleVelocity.w += dt;
//            Vector3d particlePosition = particlePositions[i];
//            newPosition.set(particleVelocity.x, particleVelocity.y, particleVelocity.z).mul(dt).add(particlePosition);
//            if (particleVelocity.w > maxParticleLifetime) {
//                particleVelocity.w = 0.0f;
//                continue;
//            }
//            particlePosition.set(newPosition);
//        }
//    }

    private void updateShots(float dt) {
        projectiles:
        for (int i = 0; i < projectilePositions.length; i++) {
            Vector4f projectileVelocity = projectileVelocities[i];
            if (projectileVelocity.w <= 0.0f)
                continue;
            projectileVelocity.w += dt;
            Vector3d projectilePosition = projectilePositions[i];
            newPosition.set(projectileVelocity.x, projectileVelocity.y, projectileVelocity.z).mul(dt).add(projectilePosition);
            if (projectileVelocity.w > maxShotLifetime) {
                projectileVelocity.w = 0.0f;
                continue;
            }
            /* Test against ships */
//            for (int r = 0; r < shipCount; r++) {
//                Ship ship = ships[r];
//                if (ship == null)
//                    continue;
//                if (broadphase(ship.x, ship.y, ship.z, this.ship.boundingSphereRadius, shipRadius, projectilePosition, newPosition)
//                        && narrowphase(this.ship.positions, ship.x, ship.y, ship.z, shipRadius, projectilePosition, newPosition, tmp, tmp2)) {
//                    emitExplosion(tmp, null);
//                    ships[r] = null;
//                    projectileVelocity.w = 0.0f;
//                    if (r == shootingShip) {
//                        for (int sr = 0; sr < shipCount; sr++) {
//                            if (ships[sr] != null) {
//                                shootingShip = sr;
//                                break;
//                            }
//                        }
//                    }
//                    continue projectiles;
//                }
//            }
            /* Test against asteroids */
//            for (int r = 0; r < asteroidCount; r++) {
//                Asteroid asteroid = asteroids[r];
//                if (asteroid == null) {
//                    continue;
//                }
//                if (broadphase(asteroid.x, asteroid.y, asteroid.z, this.asteroid.boundingSphereRadius, asteroid.scale, projectilePosition, newPosition)
//                        && narrowphase(this.asteroid.positions, asteroid.x, asteroid.y, asteroid.z, asteroid.scale, projectilePosition, newPosition, tmp, tmp2)) {
//                    emitExplosion(tmp, tmp2);
//                    projectileVelocity.w = 0.0f;
//                    continue projectiles;
//                }
//            }
            projectilePosition.set(newPosition);
        }
    }

//    private void emitExplosion(Vector3d p, Vector3f normal) {
//        int c = explosionParticles;
//        if (normal != null) {
//            GeometryUtils.perpendicular(normal, tmp4, tmp3);
//        }
//        for (int i = 0; i < particlePositions.length; i++) {
//            Vector3d particlePosition = particlePositions[i];
//            Vector4d particleVelocity = particleVelocities[i];
//            if (particleVelocity.w <= 0.0f) {
//                if (normal != null) {
//                    float r1 = (float) Math.random() * 2.0f - 1.0f;
//                    float r2 = (float) Math.random() * 2.0f - 1.0f;
//                    particleVelocity.x = normal.x + r1 * tmp4.x + r2 * tmp3.x;
//                    particleVelocity.y = normal.y + r1 * tmp4.y + r2 * tmp3.y;
//                    particleVelocity.z = normal.z + r1 * tmp4.z + r2 * tmp3.z;
//                } else {
//                    float x = (float) Math.random() * 2.0f - 1.0f;
//                    float y = (float) Math.random() * 2.0f - 1.0f;
//                    float z = (float) Math.random() * 2.0f - 1.0f;
//                    particleVelocity.x = x;
//                    particleVelocity.y = y;
//                    particleVelocity.z = z;
//                }
//                particleVelocity.normalize3();
//                particleVelocity.mul(140);
//                particleVelocity.w = 0.01f;
//                particlePosition.set(p);
//                if (c-- == 0) {
//                    break;
//                }
//            }
//        }
//    }

    private void render() {
        drawer.draw(program);
//        drawCubemap();
//        drawShots();
//        drawParticles();
//        drawHudShotDirection();
//        drawHudShip();
//        drawVelocityCompass();
    }

    public void loop() {
        try {
            while (!glfwWindowShouldClose(window)) {
                glfwPollEvents();
                glViewport(0, 0, fbWidth, fbHeight);
                Thread.sleep(80);
                update();
                render();
                glfwSwapBuffers(window);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
