package com.helloworld.faceinteract;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;

import java.util.ArrayList;
import java.util.List;

public class PhotoScanner
{
    private Bitmap bitmap;
    private byte[] nv21Data;
    private FaceDataManager manager;
    private List<AFD_FSDKFace> sdkFaces;
    public PhotoScanner(Bitmap bitmap)
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
    public List<Rect> scan()
    {
        if (manager == null)
        {
            Log.e("Null error", "PhotoScanner.manager is null");
            return new ArrayList<>();
        }
        convertToNv21();
        List<AFD_FSDKFace> faces = new ArrayList<>();
        int errorCode = manager.getFaceDetectionEngine()
                .AFD_FSDK_StillImageFaceDetection(nv21Data,
                        bitmap.getWidth(), bitmap.getHeight(),
                        AFD_FSDKEngine.CP_PAF_NV21, faces)
                .getCode();

        List<Rect> list = new ArrayList<>();
        if (errorCode == AFD_FSDKError.MOK)
        {
            for (AFD_FSDKFace face : faces)
            {
                list.add(face.getRect());
            }
            sdkFaces = faces;
        }
        else
        {
            Log.e("Error", "Face detection failed");
        }
        return list;
    }
    public Bitmap getScannedBitmap()
    {
        List<Rect> rectList = scan();
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
    public List<AFD_FSDKFace> getSdkFaces()
    {
        return sdkFaces;
    }

    public void setManager(FaceDataManager manager)
    {
        this.manager = manager;
    }
}
