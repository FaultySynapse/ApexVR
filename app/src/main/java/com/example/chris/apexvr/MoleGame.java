package com.example.chris.apexvr;

import android.opengl.Matrix;

import io.github.apexhaptics.apexhapticsdisplay.datatypes.GameStatePacket;
import io.github.apexhaptics.apexhapticsdisplay.datatypes.RobotKinPosPacket;
import io.github.apexhaptics.apexhapticsdisplay.datatypes.RobotPosPacket;

/**
 * Created by Chris on 3/12/2017.
 */

public class MoleGame {

    private static final float TABLE_HIGHT = 1.043f;
    private static final String TAG = "MOLE_GAME";
    private float[] tableLocation;
    private float[] tableRotation;
    private boolean ready = false;
    ApexGraphics graphics;

    public MoleGame(ApexGraphics graphics){
        tableLocation = new float[16];
        Matrix.setIdentityM(tableLocation,0);
        this.graphics = graphics;

    }


    public void upadte(RobotPosPacket robotPosPacket, GameStatePacket gameStatePacket, RobotKinPosPacket robotKinPosPacket){

        if(robotPosPacket != null){
            ready = true;

            tableRotation = robotPosPacket.rotMat;

            //Log.i(TAG, Arrays.toString(robotPosPacket.rotMat));

            Matrix.setIdentityM(tableLocation,0);
            Matrix.translateM(tableLocation,0, robotPosPacket.X,0.0f,robotPosPacket.Z);
            Matrix.multiplyMM(graphics.getTable().getOrientation(),0, tableLocation,0,tableRotation,0);
            Matrix.scaleM(graphics.getTable().getOrientation(),0,1.2f,1.0f,1.2f);

        }

        if(!ready){
            return;
        }

        if(graphics.getNumberOfMoles() == 0){
            for(int i = 1; i < 5; ++i){
                for(int j = 1; j < 5; ++j){
                    int mole = graphics.createMole(MoleColours.values()[(i+j)%MoleColours.values().length].getColour());

                    float[] moleTransform = new float[16];
                    Matrix.multiplyMM(moleTransform,0,tableLocation,0,tableRotation,0);
                    Matrix.translateM(graphics.getMole(mole).getOrientation(),0,moleTransform,0,
                            i/5.0f-0.5f,TABLE_HIGHT,j/5.0f-0.5f);

                    Matrix.scaleM(graphics.getMole(mole).getOrientation(),0,1.0f,(i+j)/20.0f,1.0f);

                }
            }
        }

    }

    public boolean isReady() {
        return ready;
    }

    private enum MoleColours{
        Black (new float[]{0.1f,0.1f,0.1f}),
        Red (new float[]{1.0f,0.0f,0.0f}),
        Blue (new float[]{0.0f,0.0f,1.0f}),
        Green (new float[]{0.0f,1.0f,0.0f}),
        Purple (new float[]{1.0f,0.0f,1.0f}),
        Yellow (new float[]{1.0f,1.0f,0.0f});

        private float[] colour;

        MoleColours(float[] colour){
            this.colour = colour;
        }

        public float[] getColour(){
            return colour;
        }
    }
}
