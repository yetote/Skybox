package com.example.ether.skybox;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;

import com.example.ether.openglesdemo.R;
import com.example.ether.skybox.objects.ParticleSystem;
import com.example.ether.skybox.objects.ParticlesShooter;
import com.example.ether.skybox.objects.Skybox;
import com.example.ether.skybox.programs.ParticlesShaderProgram;
import com.example.ether.skybox.programs.SkyboxShaderProgram;
import com.example.ether.skybox.utils.Geometry;
import com.example.ether.skybox.utils.MatrixHelper;
import com.example.ether.skybox.utils.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class MyRenderer implements GLSurfaceView.Renderer {
    private SkyboxShaderProgram skyboxShaderProgram;
    private Skybox skybox;
    private int skyboxTexture;
    private Context context;

    private final float[] projectMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectMatrix = new float[16];

    private ParticlesShaderProgram particlesProgram;
    private ParticleSystem particleSystem;
    private ParticlesShooter redParticlesShooter;
    private ParticlesShooter greenParticlesShooter;
    private ParticlesShooter blueParticlesShooter;
    private long globalStartTime;

    private float xRotation, yRotation;

    private int texture;

    public MyRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        final float angleVarianceInDegrees = 5f;
        final float speedVariance = 1f;

        particlesProgram = new ParticlesShaderProgram(context);
        particleSystem = new ParticleSystem(10000);
        globalStartTime = System.nanoTime();

        final Geometry.Vector particlesDirection = new Geometry.Vector(0f, 0.5f, 0f);

        redParticlesShooter = new ParticlesShooter(new Geometry.Point(-1f, 0f, 0f), particlesDirection, Color.rgb(255, 50, 5), angleVarianceInDegrees, speedVariance);
        greenParticlesShooter = new ParticlesShooter(new Geometry.Point(0f, 0f, 0f), particlesDirection, Color.rgb(25, 255, 25), angleVarianceInDegrees, speedVariance);
        blueParticlesShooter = new ParticlesShooter(new Geometry.Point(1f, 0f, 0f), particlesDirection, Color.rgb(5, 50, 255), angleVarianceInDegrees, speedVariance);

        texture = TextureHelper.loadTexture(context, R.drawable.particle_texture);

        skyboxShaderProgram = new SkyboxShaderProgram(context);
        skybox = new Skybox();
        skyboxTexture = TextureHelper.loadCubeMap(context, new int[]{
                R.drawable.left,
                R.drawable.right,
                R.drawable.bottom,
                R.drawable.top,
                R.drawable.front,
                R.drawable.back,
        });
    }

    /**
     * @param gl
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        MatrixHelper.perspectiveM(projectMatrix, 45, (float) width / (float) height, 1f, 10f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        drawSkybox();
        drawPraticles();

    }

    private void drawPraticles() {
        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;

        redParticlesShooter.addParticles(particleSystem, currentTime, 5);
        greenParticlesShooter.addParticles(particleSystem, currentTime, 5);
        blueParticlesShooter.addParticles(particleSystem, currentTime, 5);

        setIdentityM(viewMatrix, 0);
        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f);
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f);
        translateM(viewMatrix, 0, 0f, -1.5f, -5f);
        multiplyMM(viewProjectMatrix, 0, projectMatrix, 0, viewMatrix, 0);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        particlesProgram.useProgram();
        particlesProgram.setUniforms(viewProjectMatrix, currentTime, texture);
        particleSystem.bindData(particlesProgram);
        particleSystem.draw();

        glDisable(GL_BLEND);
    }

    private void drawSkybox() {
        setIdentityM(viewMatrix, 0);

        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f);
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f);
        multiplyMM(viewProjectMatrix, 0, projectMatrix, 0, viewMatrix, 0);

        skyboxShaderProgram.useProgram();
        skyboxShaderProgram.setUniform(viewProjectMatrix, skyboxTexture);
        skybox.bindData(skyboxShaderProgram);
        skybox.draw();
    }

    public void handleTouchDrag(float deltaX, float deltaY) {
        xRotation += deltaX / 60f;
        yRotation += deltaY / 60f;
        if (yRotation < -90) {
            yRotation = -90;
        } else if (yRotation > 90) {
            yRotation = 90;
        }
    }
}
