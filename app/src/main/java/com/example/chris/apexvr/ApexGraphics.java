package com.example.chris.apexvr;

import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.example.chris.apexvr.apexGL.GLError;
import com.example.chris.apexvr.apexGL.mesh.ColouredInterleavedMesh;
import com.example.chris.apexvr.apexGL.mesh.MatLib;
import com.example.chris.apexvr.apexGL.mesh.VertexMesh;
import com.example.chris.apexvr.apexGL.object.ColouredStaticObject;
import com.example.chris.apexvr.apexGL.object.GLObject;
import com.example.chris.apexvr.apexGL.object.MultiCSObject;
import com.example.chris.apexvr.apexGL.object.VertexObject;
import com.example.chris.apexvr.apexGL.shader.GLProgram;
import com.example.chris.apexvr.apexGL.shader.Shader;
import com.example.chris.apexvr.apexGL.shader.Shadow;
import com.example.chris.apexvr.apexGL.world.GroundCreater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Chris on 3/7/2017.
 */

public class ApexGraphics {

    private static final float[] LIGHT_DIR_IN_WORLD_SPACE = new float[] {0.0f, 7.f/25.f, 24.f/25.f};
    private static final String TAG = "Apex Graphics";

    private List<GLObject> glObjects;


    public ApexGraphics(){

        glObjects = new ArrayList<>(10);

    }


    public void loadAssets(AssetManager assetManager){
        GLProgram texProgram,colProgram,skyProgram,shadowProgram;

        //Textured shader
        try(Shader vertexShader = Shader.loadShader(assetManager.open("shaders/textured.vert"), GLES30.GL_VERTEX_SHADER)){
            try( Shader fragmentShader  = Shader.loadShader(assetManager.open("shaders/textured.frag"),GLES30.GL_FRAGMENT_SHADER)){
                texProgram = new GLProgram();
                texProgram.attachShader(vertexShader,fragmentShader);
            }
        } catch (IOException e) {
            Log.e(TAG,"Could not load shader: " + e.toString());
            throw new RuntimeException("Could not load shader: " + e.toString());
        }

        //Coloured shader
        try(Shader vertexShader = Shader.loadShader(assetManager.open("shaders/coloured.vert"),GLES30.GL_VERTEX_SHADER)){
            try( Shader fragmentShader  = Shader.loadShader(assetManager.open("shaders/coloured.frag"),GLES30.GL_FRAGMENT_SHADER)){
                colProgram = new GLProgram();
                colProgram.attachShader(vertexShader,fragmentShader);
            }
        } catch (IOException e) {
            Log.e(TAG,"Could not load shader: " + e.toString());
            throw new RuntimeException("Could not load shader: " + e.toString());
        }

        //sky shader
        try(Shader vertexShader = Shader.loadShader(assetManager.open("shaders/sky.vert"),GLES30.GL_VERTEX_SHADER)){
            try( Shader fragmentShader  = Shader.loadShader(assetManager.open("shaders/sky.frag"),GLES30.GL_FRAGMENT_SHADER)){
                skyProgram = new GLProgram();
                skyProgram.attachShader(vertexShader,fragmentShader);
            }
        } catch (IOException e) {
            Log.e(TAG,"Could not load shader: " + e.toString());
            throw new RuntimeException("Could not load shader: " + e.toString());
        }

        //shadow shader
        try(Shader vertexShader = Shader.loadShader(assetManager.open("shaders/shadow.vert"),GLES30.GL_VERTEX_SHADER)){
            try( Shader fragmentShader  = Shader.loadShader(assetManager.open("shaders/shadow.frag"),GLES30.GL_FRAGMENT_SHADER)){
                shadowProgram = new GLProgram();
                shadowProgram.attachShader(vertexShader,fragmentShader);
            }
        } catch (IOException e) {
            Log.e(TAG,"Could not load shader: " + e.toString());
            throw new RuntimeException("Could not load shader: " + e.toString());
        }

        //Materials
        MatLib matLib = new MatLib();
        try {
            matLib.addMatLib(assetManager.open("matlibs/tree.mtl"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open matlib file: matlibs/tree.mtl");
        }
        try {
            matLib.addMatLib(assetManager.open("matlibs/ground.mtl"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open matlib file: matlibs/graound.mtl");
        }
        try {
            matLib.addMatLib(assetManager.open("matlibs/grass.mtl"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open matlib file: matlibs/graound.mtl");
        }
        try {
            matLib.addMatLib(assetManager.open("matlibs/table.mtl"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open matlib file: matlibs/graound.mtl");
        }
        try {
            matLib.addMatLib(assetManager.open("matlibs/pillar.mtl"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open matlib file: matlibs/graound.mtl");
        }


        Shadow shadows = new Shadow(shadowProgram,
                LIGHT_DIR_IN_WORLD_SPACE,
                new Shadow.BoundingBox(new float[]{0.0f,0.0f,0.0f},new float[]{120.0f,120.0f,20.0f}));


        GroundCreater groundCreater = new GroundCreater(240.0f,200);


        groundCreater.perturb(5.0f,0.5f,6,6);

        float groudAtZero = groundCreater.maxHight(1.5f) + 0.5f;

        ColouredStaticObject ground = new ColouredStaticObject(colProgram,groundCreater.getMesh());
        Matrix.translateM(ground.getOrientation(),0,0.0f,-groudAtZero,0.0f);
        ground.setCastingShadow(true);
        ground.addExtention(shadows);
        glObjects.add(ground);


        try {
            ColouredInterleavedMesh mesh = ColouredInterleavedMesh.importOBJInterleavedMesh(assetManager.open("meshes/pillar.obj"),matLib);
            ColouredStaticObject platform = new ColouredStaticObject(colProgram,mesh);
            platform.setCastingShadow(true);
            platform.addExtention(shadows);
            glObjects.add(platform);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open obj file: meshes/pillar.obj");
        }

        try {
            ColouredInterleavedMesh mesh = ColouredInterleavedMesh.importOBJInterleavedMesh(assetManager.open("meshes/table.obj"),matLib);
            ColouredStaticObject table = new ColouredStaticObject(colProgram,mesh);
            Matrix.translateM(table.getOrientation(),0,1.0f,0.0f,0.0f);
            table.setCastingShadow(true);
            table.addExtention(shadows);
            glObjects.add(table);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open obj file: meshes/table.obj");
        }

        try {
            VertexMesh mesh = VertexMesh.importOBJInterleavedMesh(assetManager.open("meshes/sky.obj"));
            VertexObject sky = new VertexObject(skyProgram,mesh);
            Matrix.translateM(sky.getOrientation(),0,0.0f,-groudAtZero,0.0f);
            Matrix.scaleM(sky.getOrientation(),0,2,2,2);
            glObjects.add(sky);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open obj file: meshes/sky.obj");
        }


        try {
            ColouredInterleavedMesh colouredMesh = ColouredInterleavedMesh.importOBJInterleavedMesh(assetManager.open("meshes/tree.obj"),matLib);
            MultiCSObject tree = new MultiCSObject(colProgram,colouredMesh);
            Matrix.translateM(tree.getOrientation(),0,0.0f,-groudAtZero,0.0f);
            tree.setCastingShadow(true);
            tree.addExtention(shadows);

            float[] sub = new float[16];
            float[] eye = new float[16];
            Random random = new Random();

            Matrix.setIdentityM(eye,0);


            for(float i = -110; i < 110; i += 6){
                for(float j = -110; j < 110; j += 6){
                    float dist2 = i*i + j*j;
                    if(dist2 > 100.0f && dist2 < 12100.0f && random.nextFloat() > 0.4f + 0.4f * dist2 / 12100.0f){

                        float xPos = i-1.6f+3.2f*random.nextFloat();
                        float zPos = j-1.6f+3.2f*random.nextFloat();
                        float yPos = groundCreater.interpolate(xPos,zPos);

                        float[] normal = groundCreater.normal(xPos,zPos);
                        float normalAngle = (float) ((float) Math.acos(normal[1])/Math.PI*180.0f);

                        if(normalAngle > 30)
                            continue;

                        Matrix.translateM(sub,0,eye,0,xPos,yPos,zPos);

                        float rotation = random.nextFloat() * 360.0f;
                        Matrix.rotateM(sub,0,rotation,0,1.0f,0);


                        float xzScale = random.nextFloat()*0.5f + 0.75f;
                        float yScale = random.nextFloat()*0.5f + 0.75f;
                        Matrix.scaleM(sub,0,xzScale,yScale,xzScale);


                        tree.addSubObject(sub.clone());
                    }
                }
            }

            glObjects.add(tree);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open obj file: meshes/ground.obj");
        }


        try {
            ColouredInterleavedMesh colouredMesh = ColouredInterleavedMesh.importOBJInterleavedMesh(assetManager.open("meshes/grass.obj"),matLib);
            MultiCSObject grass = new MultiCSObject(colProgram,colouredMesh);
            Matrix.translateM(grass.getOrientation(),0,0.0f,-groudAtZero,0.0f);
            grass.setCastingShadow(true);
            grass.addExtention(shadows);

            float[] sub = new float[16];
            float[] eye = new float[16];
            Random random = new Random();

            Matrix.setIdentityM(eye,0);

            for(int i = 0; i < 75; ++i){
                float r = 2.0f + 20.0f * random.nextFloat();
                float angle = (float) (2.0 * Math.PI * random.nextFloat());

                float xPos = (float) (Math.cos(angle) * r);
                float zPos = (float) (Math.sin(angle) * r);
                float yPos = groundCreater.interpolate(xPos,zPos) + 0.05f;
                Matrix.translateM(sub,0,eye,0,xPos,yPos,zPos);

                float[] normal = groundCreater.normal(xPos,zPos);
                float normalAngle = (float) ((float) Math.acos(normal[1])/Math.PI*180.0f);

                if(normalAngle > 60)
                    continue;

                if(normalAngle > 0.001)
                    Matrix.rotateM(sub,0,normalAngle ,normal[2],0.0f,-normal[0]);

                float rotation = random.nextFloat() * 360.0f;
                Matrix.rotateM(sub,0,rotation,0,1.0f,0);

                float xzScale = random.nextFloat()*0.5f + 0.75f;
                float yScale = random.nextFloat()*0.5f + 0.75f;
                Matrix.scaleM(sub,0,xzScale,yScale,xzScale);

                grass.addSubObject(sub.clone());

            }


            glObjects.add(grass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open obj file: meshes/ground.obj");
        }



        shadows.createStaticShadowMap(glObjects);
    }


    public void drawEye(float[] perspective, float[] view){
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_LESS);

        GLES30.glClearColor(0.6172f, 0.0f, 0.9453f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        GLError.checkGLError(TAG,"colorParam");
        GLES30.glCullFace(GLES30.GL_BACK);

        for(GLObject glObject : glObjects){
            glObject.draw(perspective,view);
        }
    }
}