package com.helloworld.faceinteract

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Paint
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*
import kotlin.concurrent.thread

/**
 * Scan images from camera
 */
class CameraScanner
/**
 * Create [CameraScanner]
 *
 * @param context     App activity
 * @param surfaceView [SurfaceView] to show preview
 */
internal constructor(context: Context, surfaceView: SurfaceView) {
    private var engineManager: EngineManager? = null
    private var faceDataManager: FaceDataManager? = null
    private val sessionManager: SessionManager
    private val cameraCreator: CameraCreator
    private var bitmap: Bitmap? = null
    private var previewThread: Thread? = null
    private var extractedFace: Face? = null

    //Get scanned bitmap by PhotoScanner
    //Save face
    val scannedBitmap: Bitmap
        get() {
            val photoScanner = PhotoScanner(bitmap!!)
            photoScanner.setEngineManager(engineManager!!)
            photoScanner.setFaceDataManager(faceDataManager!!)
            val result = photoScanner.scannedBitmap
            extractedFace = photoScanner.extractFace()
            return result
        }

    init {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        //Create camera
        cameraCreator = CameraCreator(context, manager)
        //Set preview size
        cameraCreator.previewSize = Size(surfaceView.measuredWidth, surfaceView.measuredHeight)
        val imageAvailableListener = ImageReader.OnImageAvailableListener { imageReader ->
            //When picture is taken...
            cameraCreator.camera.close()
            val image = imageReader.acquireNextImage()
            val buffer = image.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            //Save picture data in this.bitmap
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        }
        cameraCreator.createImageReader(imageAvailableListener)
        sessionManager = SessionManager(cameraCreator, surfaceView)
    }

    /**
     * Start preview
     */
    fun start() {
        sessionManager.createPreview()
        previewThread = thread(start = true) {
            while (sessionManager.surfaceHolder == null) {
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            while (sessionManager.surfaceHolder != null) {
                //How to get preview image???
                val canvas = sessionManager.surfaceHolder!!.lockCanvas()
                if (canvas != null) {

                    val paint = Paint()
                    paint.setARGB(128, 255, 160, 0)
                    paint.isAntiAlias = true
                    paint.strokeWidth = 2.0f
                    paint.style = Paint.Style.STROKE

                    // TODO: get preview image -> scan -> draw rectangles
//                    for (rect in photoScanner!!.getRectList()!!) {
//                        canvas.drawRect(rect, paint)
//                    }
                }
            }
        }
        sessionManager.setSurfaceDestroyedHandler {
            try {
                previewThread!!.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun takePicture() {
        sessionManager.takePicture()
    }

    /**
     * Get extracted face after calling getScannedBitmap
     *
     * @return Extracted face
     */
    fun extractFace(): Face? {
        return extractedFace
    }

    fun setEngineManager(engineManager: EngineManager) {
        this.engineManager = engineManager
    }

    fun setFaceDataManager(faceDataManager: FaceDataManager) {
        this.faceDataManager = faceDataManager
    }
}

/**
 * Helper class for creating camera
 */
internal class CameraCreator
/**
 * Create [CameraCreator]
 *
 * @param context       App activity
 * @param manager [CameraManager] to create camera
 */
(context: Context?, private val manager: CameraManager?) {
    val childHandler: Handler
    private val mainHandler: Handler
    var previewSize = Size(0, 0)
    /**
     * Get active camera
     *
     * @return [CameraDevice]
     */
    lateinit var camera: CameraDevice
        private set
    /**
     * Get [ImageReader] created by createImageReader
     *
     * @return [ImageReader]
     */
    lateinit var imageReader: ImageReader
        private set

    init {
        assert(manager != null)
        assert(context != null)

        //Start background thread
        val thread = HandlerThread("Camera2")
        thread.start()
        childHandler = Handler(thread.looper)
        mainHandler = Handler(context!!.mainLooper)

        try
        //Open camera
        {
            val stateCallback = object : CameraDevice.StateCallback() {
                override fun onOpened(cameraDevice: CameraDevice) {
                    //Save the active camera
                    camera = cameraDevice
                }

                override fun onDisconnected(cameraDevice: CameraDevice) {
                    //Close current camera
                    camera.close()
                }

                override fun onError(cameraDevice: CameraDevice, i: Int) {
                    Log.e("CameraCreator", "Failed to open camera : " + i)
                }
            }
            manager!!.openCamera(
                    Integer.toString(CameraCharacteristics.LENS_FACING_BACK),
                    stateCallback, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Create [ImageReader] and set listener on image available
     *
     * @param imageAvailableListener Listener on image available
     */
    fun createImageReader(imageAvailableListener: ImageReader.OnImageAvailableListener) {
        imageReader = ImageReader.newInstance(previewSize.width, previewSize.height,
                ImageFormat.NV21, 1)
        imageReader.setOnImageAvailableListener(imageAvailableListener, mainHandler)
    }
}

/**
 * Manage sessions between camera and app
 */
internal class SessionManager
/**
 * Create [SessionManager]
 *
 * @param cameraCreator [CameraCreator] for camera data
 * @param surfaceView   [SurfaceView] to show preview
 */
(private val cameraCreator: CameraCreator, private val surfaceView: SurfaceView) : SurfaceHolder.Callback {
    var surfaceHolder: SurfaceHolder? = null
        private set
    private lateinit var previewBuilder: CaptureRequest.Builder
    private lateinit var captureSession: CameraCaptureSession
    private var surfaceDestroyedHandler: (() -> Unit)? = null

    /**
     * Create preview session
     */
    fun createPreview() {
        try {
            previewBuilder = cameraCreator.camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            val surface = surfaceView.holder.surface

            previewBuilder.addTarget(surface)

            cameraCreator.camera.createCaptureSession(
                    Arrays.asList(surface, cameraCreator.imageReader.surface),
                    object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    captureSession = cameraCaptureSession
                    try {
                        previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                        //                        previewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        val previewRequest = previewBuilder.build()
                        captureSession.setRepeatingRequest(previewRequest, null, cameraCreator.childHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }

                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Log.e("SessionManager", "Config Failed")
                }
            }, cameraCreator.childHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    /**
     * Take picture from camera
     */
    fun takePicture() {
        val takePictureRequestBuilder: CaptureRequest.Builder
        try {
            takePictureRequestBuilder = cameraCreator.camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            takePictureRequestBuilder.addTarget(cameraCreator.imageReader.surface)
            takePictureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            takePictureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            val mCaptureRequest = takePictureRequestBuilder.build()
            captureSession.capture(mCaptureRequest, null, cameraCreator.childHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        this.surfaceHolder = surfaceHolder
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {

    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        this.surfaceHolder = null
        surfaceDestroyedHandler?.invoke()
    }

    fun setSurfaceDestroyedHandler(surfaceDestroyedHandler: () -> Unit) {
        this.surfaceDestroyedHandler = surfaceDestroyedHandler
    }
}