package com.minecraftclone.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Camera {
    private static final Vector3f WORLD_UP = new Vector3f(0.0f, 1.0f, 0.0f);

    private final Matrix4f projectionMatrix;
    private final Matrix4f viewMatrix;

    private float fieldOfView;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    private final Vector3f position;
    private final Vector3f forward;
    private final Vector3f right;
    private final Vector3f up;

    private float yaw;
    private float pitch;

    public Camera(float fieldOfView, float aspectRatio, float nearPlane, float farPlane) {
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.position = new Vector3f();
        this.forward = new Vector3f(0.0f, 0.0f, -1.0f);
        this.right = new Vector3f(1.0f, 0.0f, 0.0f);
        this.up = new Vector3f(0.0f, 1.0f, 0.0f);

        this.fieldOfView = fieldOfView;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;

        updateProjectionMatrix();
        updateViewMatrix();
    }

    public void setAspectRatio(float aspect) {
        this.aspectRatio = aspect;
        updateProjectionMatrix();
    }

    public void setPosition(Vector3f newPosition) {
        this.position.set(newPosition);
        updateViewMatrix();
    }

    public void setRotation(float yawDegrees, float pitchDegrees) {
        this.yaw = yawDegrees;
        this.pitch = pitchDegrees;
        float yawRad = (float) Math.toRadians(yawDegrees);
        float pitchRad = (float) Math.toRadians(pitchDegrees);

        forward.x = (float) (Math.cos(pitchRad) * Math.cos(yawRad));
        forward.y = (float) Math.sin(pitchRad);
        forward.z = (float) (Math.cos(pitchRad) * Math.sin(yawRad));
        forward.normalize();

        right.set(forward).cross(WORLD_UP).normalize();
        up.set(right).cross(forward).normalize();

        updateViewMatrix();
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public Vector3f getForward() {
        return new Vector3f(forward);
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    private void updateProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.perspective(fieldOfView, aspectRatio, nearPlane, farPlane);
    }

    private void updateViewMatrix() {
        Vector3f center = new Vector3f(position).add(forward);
        viewMatrix.identity();
        viewMatrix.lookAt(position, center, up);
    }
}
