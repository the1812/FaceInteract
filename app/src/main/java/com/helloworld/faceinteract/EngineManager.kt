package com.helloworld.faceinteract

import android.util.Log

import com.arcsoft.facedetection.AFD_FSDKEngine
import com.arcsoft.facedetection.AFD_FSDKError
import com.arcsoft.facerecognition.AFR_FSDKEngine
import com.arcsoft.facerecognition.AFR_FSDKError
import com.arcsoft.facetracking.AFT_FSDKEngine

/**
 * Manage engines
 */
class EngineManager {

    val AppId = "9J5vsNUBj5Z52PMhwna8zN3FLoX5N7pBBgYK8tfoiRbD"
    val FaceTrackingKey = "8qdYTstWVnf2PB6HWYTVWyVZXJYKdWj2F7FJnoUmBJGH"
    val FaceDetectionKey = "8qdYTstWVnf2PB6HWYTVWyVgghoUJZtH7URmCSpZmiiq"
    val FaceRecognitionKey = "8qdYTstWVnf2PB6HWYTVWyVor74dhPTuRU2nXAdU7aEH"
    val AgeEstimationKey = "8qdYTstWVnf2PB6HWYTVWyWRf7NW4vvWzp9Mth5S7ZUe"
    val GenderEstimationKey = "8qdYTstWVnf2PB6HWYTVWyWYpWdeLKkT8hdh93wjz87G"

    val faceDetectionEngine = AFD_FSDKEngine()
    val faceRecognitionEngine = AFR_FSDKEngine()
    val faceTrackingEngine = AFT_FSDKEngine()

    /**
     * Create engines
     */
    init {

        val response = listOf(
                faceRecognitionEngine.AFR_FSDK_InitialEngine(AppId, FaceRecognitionKey).code,
                faceDetectionEngine.AFD_FSDK_InitialFaceEngine(AppId, FaceDetectionKey, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 25).code,
                faceTrackingEngine.AFT_FSDK_InitialFaceEngine(AppId, FaceTrackingKey, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 25).code)
        if (response.any { it != 0 }) {
            Log.e("Error", "Error init engine")
        } else {
            Log.d("Info", "Engine loaded")
        }
    }

    /**
     * Dispose engines
     */
    fun dispose() {
        faceRecognitionEngine.AFR_FSDK_UninitialEngine()
        faceDetectionEngine.AFD_FSDK_UninitialFaceEngine()
        faceTrackingEngine.AFT_FSDK_UninitialFaceEngine()
    }
}
