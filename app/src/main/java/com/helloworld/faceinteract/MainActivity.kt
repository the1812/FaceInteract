package com.helloworld.faceinteract

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val pickPhotoRequest = 1
    private lateinit var faceDataManager: FaceDataManager
    private lateinit var engineManager: EngineManager
    private lateinit var permissionHelper: PermissionHelper
    private var currentFace: Face? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        faceDataManager = FaceDataManager("/face-interact")
        engineManager = EngineManager()

        permissionHelper = PermissionHelper(this)

        buttonSelectPhoto.setOnClickListener {
            imageView.visibility = View.VISIBLE
            surfaceView.visibility = View.GONE
            pickPhoto()
        }
        buttonOpenCamera.setOnClickListener {
            imageView.visibility = View.GONE
            surfaceView.visibility = View.VISIBLE
            openCamera()
        }

        buttonSaveFace.setOnClickListener { showInputNameDialog() }
        buttonSaveFace.visibility = View.GONE
    }

    override fun onDestroy() {
        engineManager.dispose()
        super.onDestroy()
    }

    private fun pickPhoto() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_photo)), pickPhotoRequest)
    }

    private fun openCamera() {
        permissionHelper.requestPermission(Manifest.permission.CAMERA)
        {
            val cameraScanner = CameraScanner(this, surfaceView)
            cameraScanner.setEngineManager(engineManager)
            cameraScanner.setFaceDataManager(faceDataManager)
            cameraScanner.start()
            surfaceView.setOnClickListener {
                cameraScanner.takePicture()
                imageView!!.setImageBitmap(cameraScanner.scannedBitmap)
                imageView!!.visibility = View.VISIBLE
                surfaceView!!.visibility = View.GONE
                loadFace(cameraScanner.extractFace())
            }
        }
    }

    private fun showInputNameDialog() {
        val alert = AlertDialog.Builder(this).setTitle(R.string.save_face)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 15, 50, 15)

        val nameText = TextView(this)
        nameText.setText(R.string.name)
        val nameInput = EditText(this)

        layout.addView(nameText)
        layout.addView(nameInput)

        alert.setView(layout)
                .setPositiveButton(R.string.cancel) { _, _ -> }
                .setNegativeButton(R.string.ok) { _, _ ->
                    currentFace!!.name = nameInput.text.toString()
                    saveFace()
                }.show()
    }

    private fun saveFace() {
        if (currentFace?.name != null) {
            faceDataManager.saveFace(currentFace!!)
        }
    }

    private fun loadFace(newFace: Face?) {
        currentFace = newFace
        if (currentFace != null) {
            buttonSaveFace!!.visibility = View.GONE
            textView!!.text = currentFace!!.name
        } else {
            buttonSaveFace!!.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Result", "" + requestCode)
        if (requestCode == pickPhotoRequest &&
                resultCode == Activity.RESULT_OK &&
                data?.data != null) {
            val uri = data.data
            Log.d("Uri", uri.toString())
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val scanner = PhotoScanner(bitmap)
                scanner.setFaceDataManager(faceDataManager)
                scanner.setEngineManager(engineManager)
                imageView.setImageBitmap(scanner.scannedBitmap)
                loadFace(scanner.extractFace())
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) = permissionHelper.onRequestPermissionsResult(requestCode, grantResults)
}
