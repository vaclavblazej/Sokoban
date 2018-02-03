package cz.cvut.fit.blazeva.app.view;

import cz.cvut.fit.blazeva.app.model.EntityType;

import java.io.IOException;

import static cz.cvut.fit.blazeva.app.view.Shader.createShader;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;

public class Program {

    private static int createProgram(int vshader, int fshader) {
        int program = glCreateProgram();
        glAttachShader(program, vshader);
        glAttachShader(program, fshader);
        glLinkProgram(program);
        int linked = glGetProgrami(program, GL_LINK_STATUS);
        String programLog = glGetProgramInfoLog(program);
        if (programLog != null && programLog.trim().length() > 0) {
            System.err.println(programLog);
        }
        if (linked == 0) {
            throw new AssertionError("Could not link program");
        }
        return program;
    }

    private int cubemapProgram;
    private int cubemap_invViewProjUniform;
    private int shipProgram;
    private int shotProgram;
    private int particleProgram;

    private int[] programs = new int[EntityType.numberOfEntityTypes];
    private int[] invertedViewProjection = new int[EntityType.numberOfEntityTypes];
    private int[] viewProjection = new int[EntityType.numberOfEntityTypes];
    private int[] projection = new int[EntityType.numberOfEntityTypes];
    private int[] modelUniform = new int[EntityType.numberOfEntityTypes];

    public int program(EntityType type) {
        return programs[type.id];
    }

    public int invViewProjection(EntityType type) {
        return invertedViewProjection[type.id];
    }

    public int viewUniform(EntityType type) {
        return viewProjection[type.id];
    }

    public int projection(EntityType type) {
        return projection[type.id];
    }

    public int modelUniform(EntityType type) {
        return modelUniform[type.id];
    }

    public void initializePrograms() throws IOException {
        createCubemapProgram();
        createParticleProgram();
        createShipProgram();
        createShotProgram();
    }

    private int composeShader(String name) throws IOException {
        int vshader = createShader(name + ".vs", GL_VERTEX_SHADER);
        int fshader = createShader(name + ".fs", GL_FRAGMENT_SHADER);
        return createProgram(vshader, fshader);
    }

    private void createCubemapProgram() throws IOException {
        int program = composeShader("cubemap");
        glUseProgram(program);
        int texLocation = glGetUniformLocation(program, "tex");
        glUniform1i(texLocation, 0);
        invertedViewProjection[EntityType.CUBEMAP.id] = glGetUniformLocation(program, "invViewProj");
        glUseProgram(0);
        programs[EntityType.CUBEMAP.id] = program;
    }

    private void createShipProgram() throws IOException {
        int program = composeShader("ship");
        glUseProgram(program);
        viewProjection[EntityType.SHIP.id] = glGetUniformLocation(program, "view");
        projection[EntityType.SHIP.id] = glGetUniformLocation(program, "proj");
        modelUniform[EntityType.SHIP.id] = glGetUniformLocation(program, "model");
        glUseProgram(0);
        programs[EntityType.SHIP.id] = program;
        programs[EntityType.ASTEROID.id] = program;
    }

    private void createParticleProgram() throws IOException {
        int program = composeShader("particle");
        glUseProgram(program);
        projection[EntityType.PARTICLE.id] = glGetUniformLocation(program, "proj");
        glUseProgram(0);
        programs[EntityType.PARTICLE.id] = program;
    }

    private void createShotProgram() throws IOException {
        int program = composeShader("shot");
        glUseProgram(program);
        projection[EntityType.SHOT.id] = glGetUniformLocation(program, "proj");
        glUseProgram(0);
        programs[EntityType.SHOT.id] = program;
    }
}
