package com.helloworld.faceinteract;

import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKFace;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Face
{
    private String name;
    private List<AFR_FSDKFace> faces;
    public Face(String name)
    {
        this.name = name;
        faces = new ArrayList<>();
    }
    public Face(String name, AFR_FSDKFace sdkFace)
    {
        this(name);
        faces.add(sdkFace);
    }
    public Face(String name, AFD_FSDKFace detectionFace)
    {

    }
    public String getName()
    {
        return name;
    }
    public void setName(String name) { this.name = name; }
    public void addSdkFace(AFR_FSDKFace sdkFace)
    {
        faces.add(sdkFace);
    }
    public AFR_FSDKFace getFirstSdkFace()
    {
        if (faces.size() == 0)
        {
            return null;
        }
        return faces.get(0);
    }
}
