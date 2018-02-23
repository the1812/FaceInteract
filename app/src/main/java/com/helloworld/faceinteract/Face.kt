package com.helloworld.faceinteract

import com.arcsoft.facerecognition.AFR_FSDKFace

/**
 * Stores name of face and multiple face data.
 */
class Face(var name: String) {
    private val faces = arrayListOf<AFR_FSDKFace>()

    val firstSdkFace: AFR_FSDKFace?
        get() = try {
            faces.first()
        } catch (e: NoSuchElementException) {
            null
        }

    constructor(name: String, sdkFace: AFR_FSDKFace) : this(name) {
        faces.add(sdkFace)
    }

    fun addSdkFace(sdkFace: AFR_FSDKFace?) {
        if (sdkFace != null) faces.add(sdkFace)
    }
}
