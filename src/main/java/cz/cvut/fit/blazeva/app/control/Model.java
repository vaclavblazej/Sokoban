package cz.cvut.fit.blazeva.app.control;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GLCapabilities;

public class Model {

    public static boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST];
    public static boolean leftMouseDown = false;
    public static boolean rightMouseDown = false;
    public static float mouseX = 0.0f;
    public static float mouseY = 0.0f;
    public static GLCapabilities caps;
    public static int width = 800;
    public static int height = 600;
    public static int fbWidth = 800;
    public static int fbHeight = 600;
    public static long window;

}
