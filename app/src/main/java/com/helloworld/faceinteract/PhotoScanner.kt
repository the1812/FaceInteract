package com.helloworld.faceinteract

import android.graphics.*
import android.util.Log
import com.arcsoft.facedetection.AFD_FSDKEngine
import com.arcsoft.facedetection.AFD_FSDKError
import com.arcsoft.facedetection.AFD_FSDKFace
import com.arcsoft.facerecognition.AFR_FSDKEngine
import com.arcsoft.facerecognition.AFR_FSDKError
import com.arcsoft.facerecognition.AFR_FSDKFace
import com.arcsoft.facerecognition.AFR_FSDKMatching

/**
 * Scan faces in a photo
 */
class PhotoScanner internal constructor(private val bitmap: Bitmap) {
    private var nv21Data = ByteArray(bitmap.width * bitmap.height * 3 / 2)
    private var engineManager: EngineManager? = null
    private var faceDataManager: FaceDataManager? = null
    private var rectList: MutableList<Rect> = ArrayList()
    private var sdkFaces: List<AFD_FSDKFace> = ArrayList()
    private var infoList: MutableList<String> = ArrayList()
    /**
     * Minimum score for face recognition
     */
    private val matchMinimumScore = 0.7f

    // Create new bitmap
    //Draw original bitmap
    //Draw face rectangle on bitmap
    val scannedBitmap: Bitmap
        get() {
            scan()
            val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
            val canvas = Canvas(result)
            val paint = Paint()
            paint.setARGB(128, 255, 160, 0)
            paint.isAntiAlias = true
            paint.strokeWidth = 2.0f
            paint.style = Paint.Style.STROKE
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            for (rect in rectList) {
                canvas.drawRect(rect, paint)
            }
            return bitmap
        }

    /**
     * Convert this.bitmap to NV21 format and save in this.nv21Data
     */
    private fun convertToNv21() {
        val width = bitmap.width
        val height = bitmap.height
        val frameSize = width * height
        val argb = IntArray(frameSize)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)
        nv21Data = ByteArray(frameSize * 3 / 2)
        var yIndex = 0
        var uvIndex = frameSize

        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {

                val r = argb[index] and 0xff0000 shr 16
                val g = argb[index] and 0xff00 shr 8
                val b = argb[index] and 0xff

                val y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                val u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                val v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128

                nv21Data[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    nv21Data[uvIndex++] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                    nv21Data[uvIndex++] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                }
                index++
            }
        }
    }

    /**
     * Start scanning
     */
    private fun scan() {
        if (engineManager == null) {
            Log.e("Null error", "PhotoScanner.engineManager is null")
            return
        }
        convertToNv21()
        sdkFaces = ArrayList()
        val errorCode = engineManager!!.faceDetectionEngine
                .AFD_FSDK_StillImageFaceDetection(nv21Data,
                        bitmap.width, bitmap.height,
                        AFD_FSDKEngine.CP_PAF_NV21, sdkFaces)
                .code
        sdkFaces = ArrayList(sdkFaces)
        rectList = ArrayList()
        infoList = ArrayList()
        if (errorCode == AFD_FSDKError.MOK) {
            for (sdkFace in sdkFaces) {
                rectList.add(sdkFace.rect)
                infoList.add(match(toRecognitionFace(sdkFace)))
            }
        } else {
            Log.e("Error", "Face detection failed")
        }
    }

    /**
     * Find name of specific face
     *
     * @param sdkFace Specific face
     * @return Name of face, null if not found
     */
    private fun match(sdkFace: AFR_FSDKFace?): String {
        if (sdkFace == null || engineManager == null)
        {
            return ""
        }
        for (face in faceDataManager!!.faces) {
            val matching = AFR_FSDKMatching()
            val errorCode = engineManager!!.faceRecognitionEngine
                    .AFR_FSDK_FacePairMatching(face.firstSdkFace, sdkFace, matching)
                    .code
            if (errorCode == AFR_FSDKError.MOK) {
                if (matching.score >= matchMinimumScore) {
                    return face.name
                }
            } else {
                Log.e("Error", "Face matching failed")
            }
        }
        return ""
    }

    /**
     * Convert detection face to recognition face
     *
     * @param detectionFace Detection face
     * @return Recognition face
     */
    private fun toRecognitionFace(detectionFace: AFD_FSDKFace): AFR_FSDKFace? {
        if (engineManager == null)
        {
            return null
        }
        val recognitionFace = AFR_FSDKFace()
        val errorCode = engineManager!!.faceRecognitionEngine
                .AFR_FSDK_ExtractFRFeature(nv21Data,
                        bitmap.width, bitmap.height,
                        ImageFormat.NV21, detectionFace.rect,
                        AFR_FSDKEngine.AFR_FOC_0, recognitionFace)
                .code
        return if (errorCode == AFR_FSDKError.MOK)
        {
            recognitionFace
        }
        else
        {
            Log.e("Photo Scanner", "Convert failed")
            null
        }
    }

//    fun getRectList(): List<Rect>? {
//        return rectList
//    }

    fun setEngineManager(engineManager: EngineManager) {
        this.engineManager = engineManager
    }

    fun setFaceDataManager(faceDataManager: FaceDataManager) {
        this.faceDataManager = faceDataManager
    }

    /**
     * Extract [Face] using the scanned data
     *
     * @return Face
     */
    fun extractFace(): Face? {
        return if (sdkFaces.isEmpty()) null else Face(infoList[0], toRecognitionFace(sdkFaces[0])!!)
    }

}
