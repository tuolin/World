package main;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by IntelliJ IDEA.
 * User: tuolin
 * Date: 12-1-7
 * Time: PM10:42
 * To change this template use File | Settings | File Templates.
 */
public class Camera {
    private Vector3f position = null;
    private float yaw = 0;
    private float pitch = 0;

    public Camera(float x, float y, float z) {
       position = new Vector3f(-x, -y, -z);
    }

    public void yaw(float amount) {
        yaw += amount;
    }

    public void pitch(float amount) {
        pitch += amount;
    }

    public void forward(float d) {
        position.x -= d * Math.sin(Math.toRadians(yaw));
        position.z += d * Math.cos(Math.toRadians(yaw));
    }

    public void backward(float d) {
        position.x += d * Math.sin(Math.toRadians(yaw));
        position.z -= d * Math.cos(Math.toRadians(yaw));
    }

    public void left (float d) {
        position.x -= d * (float)Math.sin(Math.toRadians(yaw-90));
        position.z += d * (float)Math.cos(Math.toRadians(yaw-90));
    }

    public void right(float d) {
        position.x -= d * (float)Math.sin(Math.toRadians(yaw+90));
        position.z += d * (float)Math.cos(Math.toRadians(yaw+90));
    }

    public void up(float d) {
        position.y -= d;
    }

    public void down(float d) {
        position.y += d;
    }

    public void look() {
        //roatate the pitch around the X axis
        GL11.glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        //roatate the yaw around the Y axis
        GL11.glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        //translate to the position vector's location
        GL11.glTranslatef(position.x, position.y, position.z);
    }

    public Vector3f position() {
        return new Vector3f(-position.getX(), -position.getY(), -position.getZ());
    }
}
