package com.helloworld.faceinteract;

import android.util.Log;
import com.arcsoft.facerecognition.AFR_FSDKFace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FaceDataManager
{

    private final String fileName = "face.txt";
    private String storagePath;
    private List<Face> faces;

    FaceDataManager(String storagePath)
    {
        Log.d("Manager", "constructor start");
        this.storagePath = storagePath;
        faces = new ArrayList<>();

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
        TextFile textFile = new TextFile(storagePath + "/" + fileName).load();
        if (!textFile.exists())
        {
            Log.e("Error","File not exist");
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
        TextFile textFile = new TextFile(storagePath + "/" + fileName).load();
        if (!textFile.exists())
        {
            Log.e("Error","File not exist");
        }
        return Arrays.asList(textFile.getText().split("\n"));
    }
    public List<Face> getFaces()
    {
        return faces;
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
            BinaryFile binaryFile = new BinaryFile(storagePath + "/" + name).load();
            AFR_FSDKFace sdkFace = new AFR_FSDKFace();
            sdkFace.setFeatureData(binaryFile.getData());
            faces.add(new Face(name, sdkFace));
        }
    }
}
