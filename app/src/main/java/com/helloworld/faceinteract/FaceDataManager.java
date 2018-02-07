package com.helloworld.faceinteract;

import android.util.Log;
import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.facetracking.AFT_FSDKEngine;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FaceDataManager
{
    public final String AppId = "9J5vsNUBj5Z52PMhwna8zN3FLoX5N7pBBgYK8tfoiRbD";
    public final String FaceTrackingKey = "8qdYTstWVnf2PB6HWYTVWyVZXJYKdWj2F7FJnoUmBJGH";
    public final String FaceDetectionKey = "8qdYTstWVnf2PB6HWYTVWyVgghoUJZtH7URmCSpZmiiq";
    public final String FaceRecognitionKey = "8qdYTstWVnf2PB6HWYTVWyVor74dhPTuRU2nXAdU7aEH";
    public final String AgeEstimationKey  = "8qdYTstWVnf2PB6HWYTVWyWRf7NW4vvWzp9Mth5S7ZUe";
    public final String GenderEstimationKey = "8qdYTstWVnf2PB6HWYTVWyWYpWdeLKkT8hdh93wjz87G";

    private final String fileName = "face.txt";
    private String storagePath;
    private List<Face> faces;
    private AFD_FSDKEngine faceDetectionEngine;
    private AFR_FSDKEngine faceRecognitionEngine;
    private AFT_FSDKEngine faceTrackingEngine;

    public AFR_FSDKEngine getFaceRecognitionEngine()
    {
        return faceRecognitionEngine;
    }

    public AFD_FSDKEngine getFaceDetectionEngine()
    {
        return faceDetectionEngine;
    }

    public AFT_FSDKEngine getFaceTrackingEngine()
    {
        return faceTrackingEngine;
    }

    public FaceDataManager(String storagePath)
    {
        Log.d("Manager", "constructor start");
        this.storagePath = storagePath;
        faces = new ArrayList<>();
        faceRecognitionEngine = new AFR_FSDKEngine();
        faceDetectionEngine = new AFD_FSDKEngine();
        faceTrackingEngine = new AFT_FSDKEngine();

        boolean error = faceRecognitionEngine
                .AFR_FSDK_InitialEngine(AppId, FaceRecognitionKey)
                .getCode() != AFR_FSDKError.MOK;
        error = error && (faceDetectionEngine
                .AFD_FSDK_InitialFaceEngine(AppId, FaceDetectionKey,
                        AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16,25)
                .getCode() != AFD_FSDKError.MOK);
        error = error && (faceTrackingEngine
                .AFT_FSDK_InitialFaceEngine(AppId, FaceTrackingKey,
                        AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16,25)
                .getCode() != 0);
        if (error)
        {
            Log.e("Error", "Error init engine");
        }
        else
        {
            Log.d("Info", "Engine loaded");
        }
    }
    public void dispose()
    {
        if (faceRecognitionEngine != null)
        {
            faceRecognitionEngine.AFR_FSDK_UninitialEngine();
        }
        if (faceDetectionEngine != null)
        {
            faceDetectionEngine.AFD_FSDK_UninitialFaceEngine();
        }
        if (faceTrackingEngine != null)
        {
            faceTrackingEngine.AFT_FSDK_UninitialFaceEngine();
        }
    }
    private String getDataFileName(Face face)
    {
        return face.getName() + ".data";
    }
    private Face getRegisteredFace(Face face)
    {
        for (Face f : faces)
        {
            if (f.getName().equals(face.getName()))
            {
                return face;
            }
        }
        return null;
    }
    private void saveToNameList(String name)
    {
        TextFile textFile = new TextFile(storagePath + "/" + fileName);
        if (textFile.exists())
        {
            try
            {
                textFile.load();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        if (Arrays.asList(textFile.getText().split("\n")).contains(name))
        {
            return;
        }
        textFile.appendText(name + "\n");
        textFile.save();
    }
    private List<String> getNameList()
    {
        TextFile textFile = new TextFile(storagePath + "/" + fileName);
        if (textFile.exists())
        {
            try
            {
                textFile.load();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        return Arrays.asList(textFile.getText().split("\n"));
    }
    public void saveFace(Face face)
    {
        Face registeredFace = getRegisteredFace(face);
        if (registeredFace == null)
        {
            registeredFace = new Face(face.getName());
            saveToNameList(face.getName());
        }
        AFR_FSDKFace sdkFace = face.getFirstSdkFace();
        registeredFace.addSdkFace(sdkFace);

        BinaryFile binaryFile = new BinaryFile(storagePath + "/" + getDataFileName(face));
        binaryFile.setData(sdkFace.getFeatureData());
        binaryFile.save();
    }
    public void loadFaces()
    {
        faces.clear();
        List<String> names = getNameList();
        for (String name : names)
        {
            try
            {
                BinaryFile binaryFile = new BinaryFile(storagePath + "/" + name);
                binaryFile.load();
                AFR_FSDKFace sdkFace = new AFR_FSDKFace();
                sdkFace.setFeatureData(binaryFile.getData());
                faces.add(new Face(name, sdkFace));
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }
}
