package com.helloworld.faceinteract;

import android.util.Log;
import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKEngine;

public class EngineManager
{

    public final String AppId = "9J5vsNUBj5Z52PMhwna8zN3FLoX5N7pBBgYK8tfoiRbD";
    public final String FaceTrackingKey = "8qdYTstWVnf2PB6HWYTVWyVZXJYKdWj2F7FJnoUmBJGH";
    public final String FaceDetectionKey = "8qdYTstWVnf2PB6HWYTVWyVgghoUJZtH7URmCSpZmiiq";
    public final String FaceRecognitionKey = "8qdYTstWVnf2PB6HWYTVWyVor74dhPTuRU2nXAdU7aEH";
    public final String AgeEstimationKey  = "8qdYTstWVnf2PB6HWYTVWyWRf7NW4vvWzp9Mth5S7ZUe";
    public final String GenderEstimationKey = "8qdYTstWVnf2PB6HWYTVWyWYpWdeLKkT8hdh93wjz87G";

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

    public EngineManager()
    {
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
}
