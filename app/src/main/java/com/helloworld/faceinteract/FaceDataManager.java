package com.helloworld.faceinteract;

import android.util.Log;
import com.arcsoft.facerecognition.AFR_FSDKFace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manage face data storage
 */
public class FaceDataManager
{

    private final String fileName = "face.txt";
    private String storagePath;
    private List<Face> faces;

    /**
     * Create FaceDataManager
     * @param storagePath Storage path for face data
     */
    FaceDataManager(String storagePath)
    {
        Log.d("Manager", "constructor start");
        this.storagePath = storagePath;
        faces = new ArrayList<>();
        loadFaces();
    }

    /**
     * Get name of data file
     * @param face Face that containing the name
     * @return Name of data file
     */
    private String getDataFileName(Face face)
    {
        return face.getName() + ".data";
    }

    /**
     * Find saved face that match the current face
     * @param face Current face
     * @return Saved matching face, null if not found
     */
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

    /**
     * Save current name to name list
     * @param name Name to save
     */
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

    /**
     * Get all faces' name
     * @return List of faces's name
     */
    private List<String> getNameList()
    {
        TextFile textFile = new TextFile(storagePath + "/" + fileName).load();
        if (!textFile.exists())
        {
            Log.e("Error","File not exist");
        }
        return Arrays.asList(textFile.getText().split("\n"));
    }

    /**
     * Get all faces
     * @return Face list
     */
    public List<Face> getFaces()
    {
        return faces;
    }

    /**
     * Save face to storage
     * @param face Face to save
     */
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

    /**
     * Load face data from storage
     */
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
