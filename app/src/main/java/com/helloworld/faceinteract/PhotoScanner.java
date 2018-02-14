package com.helloworld.faceinteract;

import android.graphics.*;
import android.util.Log;
import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;

import java.util.ArrayList;
import java.util.List;

public class PhotoScanner
{
    public static float MatchMinimumScore = 0.7f;

    private Bitmap bitmap;
    private byte[] nv21Data;
    private EngineManager engineManager;
    private FaceDataManager faceDataManager;
    private List<Rect> rectList;
    private List<AFD_FSDKFace> sdkFaces;
    private List<String> infoList;

    PhotoScanner(Bitmap bitmap)
    {
        this.bitmap = bitmap;
    }
    private void convertToNv21()
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int frameSize = width * height;
        int[] argb = new int[frameSize];
        bitmap.getPixels(argb,0,width,0,0,width,height);
        nv21Data = new byte[frameSize*3/2];
        int yIndex = 0;
        int uvIndex = frameSize;

        int R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff);

                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                nv21Data[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    nv21Data[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                    nv21Data[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                }
                index ++;
            }
        }
    }
    private void scan()
    {
        if (engineManager == null)
        {
            Log.e("Null error", "PhotoScanner.engineManager is null");
            return;
        }
        convertToNv21();
        sdkFaces = new ArrayList<>();
        int errorCode = engineManager.getFaceDetectionEngine()
                .AFD_FSDK_StillImageFaceDetection(nv21Data,
                        bitmap.getWidth(), bitmap.getHeight(),
                        AFD_FSDKEngine.CP_PAF_NV21, sdkFaces)
                .getCode();

        rectList = new ArrayList<>();
        infoList = new ArrayList<>();
        if (errorCode == AFD_FSDKError.MOK)
        {
            for (AFD_FSDKFace sdkFace : sdkFaces)
            {
                rectList.add(sdkFace.getRect());
                infoList.add(match(toRecognitionFace(sdkFace)));
            }
        }
        else
        {
            Log.e("Error", "Face detection failed");
        }
    }
    private String match(AFR_FSDKFace sdkFace)
    {
        for (Face face : faceDataManager.getFaces())
        {
            AFR_FSDKMatching matching = new AFR_FSDKMatching();
            int errorCode = engineManager.getFaceRecognitionEngine()
                    .AFR_FSDK_FacePairMatching(face.getFirstSdkFace(), sdkFace, matching)
                    .getCode();
            if (errorCode == AFR_FSDKError.MOK)
            {
                if (matching.getScore() >= MatchMinimumScore)
                {
                    return face.getName();
                }
            }
            else
            {
                Log.e("Error", "Face matching failed");
            }
        }
        return null;
    }
    private AFR_FSDKFace toRecognitionFace(AFD_FSDKFace detectionFace)
    {
        AFR_FSDKFace recognitionFace = new AFR_FSDKFace();
        int errorCode = engineManager.getFaceRecognitionEngine()
                .AFR_FSDK_ExtractFRFeature(nv21Data,
                        bitmap.getWidth(), bitmap.getHeight(),
                        ImageFormat.NV21, detectionFace.getRect(),
                        AFR_FSDKEngine.AFR_FOC_0, recognitionFace)
                .getCode();
        if (errorCode == AFR_FSDKError.MOK)
        {
            return recognitionFace;
        }
        else
        {
            Log.e("Photo Scanner", "Convert failed");
            return null;
        }
    }
    public Bitmap getScannedBitmap()
    {
        scan();

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setARGB(128,255,160,0);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2.0f);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawBitmap(bitmap,0,0,null);
        for (Rect rect : rectList)
        {
            canvas.drawRect(rect, paint);
        }
        return bitmap;
    }

    public void setEngineManager(EngineManager engineManager)
    {
        this.engineManager = engineManager;
    }

    public void setFaceDataManager(FaceDataManager faceDataManager)
    {
        this.faceDataManager = faceDataManager;
    }

    public Face extractFace()
    {
        return new Face(infoList.get(0), sdkFaces.get(0));
    }
}
