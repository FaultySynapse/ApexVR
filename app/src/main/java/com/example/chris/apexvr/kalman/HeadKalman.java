package com.example.chris.apexvr.kalman;

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import org.ejml.simple.SimpleMatrix;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.github.apexhaptics.apexhapticsdisplay.datatypes.HeadPacket;
import io.github.apexhaptics.apexhapticsdisplay.datatypes.JointPacket;

/**
 * Created by Chris on 3/7/2017.
 */

public class HeadKalman {

    private static final String TAG = "Apex_Kalman";
    float[] headPos = new float[]{0.0f,1.8f,0.0f};
    float[] orientation = new float[]{0.0f,0.0f,0.0f,1.0f};


    private ConcurrentLinkedQueue<HeadPacket> headQueue;
    private ConcurrentLinkedQueue<JointPacket> jointQueue;

    private SimpleMatrix A;
    private SimpleMatrix C;
    private SimpleMatrix R;
    private SimpleMatrix Q;

    private SimpleMatrix x;
    private SimpleMatrix P;


    private Long frameTime;

    private boolean ready;

    public HeadKalman(){
        headQueue = new ConcurrentLinkedQueue<>();
        jointQueue = new ConcurrentLinkedQueue<>();

        A = new SimpleMatrix(3,3);
        A.set(0,0,1);
        A.set(1,1,1);
        A.set(2,2,1);

        C = new SimpleMatrix(2,3);
        C.setRow(0,0,1,0,0);
        C.setRow(1,0,1,0,1);

        R = new SimpleMatrix(2,2);
        R.set(0,0,0.0157);
        R.set(1,1,0.1571);

        Q = new SimpleMatrix(3,3);
        Q.set(0,0,0.1571);
        Q.set(1,1,1.5708);
        Q.set(2,2,0.0314);



    }

    public void step(float[] orientation){

        if(!ready){

            ready = headQueue.isEmpty() || jointQueue.isEmpty();

            if(ready){
                frameTime = SystemClock.currentThreadTimeMillis();

                x = new SimpleMatrix(3,1);
                x.set(0,extractYaw(orientation));

                //TODO:set initial yaw offset

                P = new SimpleMatrix(3,3);
                P.set(0,0,0.0157);
                P.set(1,1,3.1416);
                P.set(2,2,0.1571);

            }

            return;
        }

        long time = SystemClock.currentThreadTimeMillis();
        float dt = 1000*(time - frameTime);
        frameTime = time;
        A.set(0,1,dt);

        x.set(A.mult(x));


    }

    public float[] getHeadTransform(){

        float[] camera = quat2Mat(orientation);

        Matrix.translateM(camera,0, -headPos[0], -headPos[1], -headPos[2]);

        return camera;
    }

    private float extractYaw(float[] transform){

        return (float) Math.atan2(transform[openGlMatrixIndex(0,2)],transform[openGlMatrixIndex(2,2)]);

    }
    private int openGlMatrixIndex(int m, int n){
        return m * 4 + n;
    }

    public ConcurrentLinkedQueue<HeadPacket> getHeadQueue() {
        return headQueue;
    }

    public ConcurrentLinkedQueue<JointPacket> getJointQueue() {
        return jointQueue;
    }

    public boolean isReady() {
        return ready;
    }

    private float[] quat2Mat(float[] quaternion){

        float qx = quaternion[0];
        float qy = quaternion[1];
        float qz = quaternion[2];
        float qw = -quaternion[3];

        return  new float[]{
                1 - 2*qy*qy - 2*qz*qz,
                2*qx*qy + 2*qz*qw,
                2*qx*qz - 2*qy*qw,
                0.0f,

                2*qx*qy - 2*qz*qw,
                1 - 2*qx*qx - 2*qz*qz,
                2*qy*qz + 2*qx*qw,
                0.0f,

                2*qx*qz + 2*qy*qw,
                2*qy*qz - 2*qx*qw,
                1 - 2*qx*qx - 2*qy*qy,
                0.0f,

                0.0f,
                0.0f,
                0.0f,
                1.0f
        };
    }


}
