package com.example.ether.skybox;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private GLSurfaceView glSurfaceView;
    private static final String TAG = "MainActivity";
    private boolean rendererSet;
    private float[] r = new float[9];
    private float[] values = new float[3];
    private SensorManager sensorManager;
    private Sensor accelerometer;//加速度传感器
    private Sensor magneticField;//磁场传感器

    private float[] accelerometerArr = new float[3];//加速度传感器数组
    private float[] magneticFieldArr = new float[3];//磁场传感器数组
    MyRenderer renderer;
    private float accX;
    private float previousAccX;
    private float previousX = 0f, deltaX;
    private float accY;
    private float previousAccY;
    private float previousY = 0f, deltaY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        renderer = new MyRenderer(this);

        glSurfaceView = new GLSurfaceView(this);

        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager
                .getDeviceConfigurationInfo();

        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));

        if (supportsEs2) {
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setRenderer(renderer);
            rendererSet = true;
        } else {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

//        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
//            float previousX, previousY;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event != null) {
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        previousX = event.getX();
//                        previousY = event.getY();
//                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                        final float deltaX = event.getX() - previousX;
//                        final float deltaY = event.getY() - previousY;
//                        previousX = event.getX();
//                        previousY = event.getY();
//                        glSurfaceView.queueEvent(new Runnable() {
//                            @Override
//                            public void run() {
//                                renderer.handleTouchDrag(deltaX, deltaY);
//                            }
//                        });
//                    }
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        setContentView(glSurfaceView);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (rendererSet) {
            glSurfaceView.onPause();
        }
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet) {
            glSurfaceView.onResume();
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerArr = event.values;
                accX = accelerometerArr[0];
                if (Math.abs(accX) > 0.2f && (accX - previousAccX) != 0) {
                    deltaX = (float) (previousX + accX * 0.02);
                    renderer.handleTouchDrag(deltaX, 0);
                    if ((accX > 0 && previousAccX < 0) || (accX < 0 && previousAccX > 0)) {
                        accX = previousAccX = 0;
                        deltaX = previousX = 0;
                    }
                    previousAccX = accX;
                    previousX = deltaX;

                }
//                accY = accelerometerArr[1];
//                if (Math.abs(accY) > 0.2f && (accY - previousAccY) !=0f) {
//                Log.e(TAG, "加速度: " + accY);
//                Log.e(TAG, "速度: " + previousAccY);
//
//                deltaY = (float) (previousY + accY * 0.02);
//                renderer.handleTouchDrag(0, -deltaY);
//                if ((accY > 0 && previousAccY < 0) || (accY < 0 && previousAccY > 0)) {
//                    accY = previousAccY = 0;
//                    deltaY = previousY = 0;
//                }
//                previousAccY = accY;
//                previousY = deltaY;

//            }

                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticFieldArr = event.values;
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
