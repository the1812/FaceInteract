package com.helloworld.faceinteract

import android.util.Log
import com.arcsoft.facerecognition.AFR_FSDKFace

/**
 * Manage face data storage
 * Create FaceDataManager
 *
 * @param storagePath Storage path for face data
 */
class FaceDataManager(private val storagePath: String) {
    private val fileName = "face.txt"
    val faces = arrayListOf<Face>()

    init {
        Log.d("Manager", "constructor start")
        loadFaces()
    }

    /**
     * Get name of data file
     *
     * @param face Face that containing the name
     * @return Name of data file
     */
    private fun getDataFileName(face: Face) = face.name + ".data"

    /**
     * Find saved face that match the current face
     *
     * @param face Current face
     * @return Saved matching face, null if not found
     */
    private fun getRegisteredFace(face: Face) = faces.find { it.name == face.name }

    /**
     * Get all faces' name
     *
     * @return List of faces's name
     */
    private fun getNameList(): List<String> {
        val textFile = TextFile(storagePath + "/" + fileName).load()
        if (!textFile.exists()) {
            Log.e("Error", "File not exist")
        }
        return textFile.text.split("\n")
    }

    /**
     * Save current name to name list
     *
     * @param name Name to save
     */
    private fun saveToNameList(name: String) {
        val textFile = TextFile(storagePath + "/" + fileName).load()
        if (!textFile.exists()) {
            Log.e("Error", "File not exist")
        }
        if (textFile.text.split("\n").contains(name)) {
            return
        }
        textFile.appendText("$name\n")
        textFile.save()
    }

    /**
     * Save face to storage
     *
     * @param face Face to save
     */
    fun saveFace(face: Face) {
        val registeredFace = getRegisteredFace(face)
                ?: Face(face.name).also { saveToNameList(face.name) }
        val sdkFace = face.firstSdkFace
        registeredFace.addSdkFace(sdkFace)

        val binaryFile = BinaryFile(storagePath + "/" + getDataFileName(face))
        binaryFile.data = sdkFace!!.featureData
        binaryFile.save()
    }

    /**
     * Load face data from storage
     */
    private fun loadFaces() {
        faces.clear()
        val names = getNameList()
        for (name in names) {
            val binaryFile = BinaryFile(storagePath + "/" + name).load()
            val sdkFace = AFR_FSDKFace()
            sdkFace.featureData = binaryFile.data
            faces.add(Face(name, sdkFace))
        }
    }
}
