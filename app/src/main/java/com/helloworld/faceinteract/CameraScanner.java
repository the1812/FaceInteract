package com.helloworld.faceinteract;

import android.content.Context;
import android.graphics.*;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Scan images from camera
 */
public class CameraScanner
{
    private EngineManager engineManager;
    private FaceDataManager faceDataManager;
    private SessionManager sessionManager;
    private PhotoScanner photoScanner;
    private CameraCreator cameraCreator;
    private Bitmap bitmap;
    private Bitmap previewBitmap;
    private Face extractedFace;

    /**
     * Create {@link CameraScanner}
     * @param context App activity
     * @param textureView {@link TextureView} to show preview
     */
    CameraScanner(Context context, final TextureView textureView)
    {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        //Create camera
        cameraCreator = new CameraCreator(context, manager);
        //Set preview size
        cameraCreator.setPreviewSize(new Size(textureView.getMeasuredWidth(), textureView.getMeasuredHeight()));
        ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener()
        {
            @Override
            public void onImageAvailable(ImageReader imageReader)
            {
                //When picture is taken...
                cameraCreator.getCamera().close();
                Image image = imageReader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                //Save picture data in this.bitmap
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        };
        cameraCreator.createImageReader(imageAvailableListener);
        sessionManager = new SessionManager(cameraCreator, textureView);
    }

    /**
     * Start preview
     */
    public void start()
    {
        sessionManager.createPreview();
    }

    public void takePicture()
    {
        sessionManager.takePicture();
    }

    public Bitmap getScannedBitmap()
    {
        photoScanner = new PhotoScanner(bitmap);
        photoScanner.setEngineManager(engineManager);
        photoScanner.setFaceDataManager(faceDataManager);
        //Get scanned bitmap by PhotoScanner
        Bitmap result = photoScanner.getScannedBitmap();
        //Save face
        extractedFace = photoScanner.extractFace();
        return result;
    }

    /**
     * Get scanned rectangles for preview
     * @return Scanned rectangles in transparent background
     */
    public Bitmap getScannedRects()
    {
        Log.e("Camera Scanner", "TODO: Create custom preview and save preivew image to this.previewBitmap");
        Bitmap result = Bitmap.createBitmap(
                previewBitmap.getWidth(),
                previewBitmap.getHeight(),
                previewBitmap.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setARGB(128,255,160,0);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2.0f);
        paint.setStyle(Paint.Style.STROKE);

        //Transparent background
        canvas.drawColor(Color.TRANSPARENT);
        for (Rect rect : photoScanner.getRectList())
        {
            canvas.drawRect(rect, paint);
        }
        return result;
    }

    /**
     * Get extracted face after calling getScannedBitmap
     * @return Extracted face
     */
    public Face extractFace()
    {
        return extractedFace;
    }
    public void setEngineManager(EngineManager engineManager)
    {
        this.engineManager = engineManager;
    }

    public void setFaceDataManager(FaceDataManager faceDataManager)
    {
        this.faceDataManager = faceDataManager;
    }
}

/**
 * Helper class for creating camera
 */
class CameraCreator
{
    private CameraManager manager;
    private Handler childHandler, mainHandler;
    private Size previewSize;
    private CameraDevice camera;
    private ImageReader imageReader;

    /**
     * Create {@link CameraCreator}
     * @param context App activity
     * @param cameraManager {@link CameraManager} to create camera
     */
    CameraCreator(Context context, CameraManager cameraManager)
    {
        assert manager != null;
        assert context != null;
        manager = cameraManager;

        //Start background thread
        HandlerThread thread = new HandlerThread("Camera2");
        thread.start();
        childHandler = new Handler(thread.getLooper());
        mainHandler = new Handler(context.getMainLooper());

        try //Open camera
        {
            CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback()
            {
                @Override
                public void onOpened(CameraDevice cameraDevice)
                {
                    //Save the active camera
                    camera = cameraDevice;
                }

                @Override
                public void onDisconnected(CameraDevice cameraDevice)
                {
                    //Close current camera
                    camera.close();
                    camera = null;
                }

                @Override
                public void onError(CameraDevice cameraDevice, int i)
                {
                    Log.e("CameraCreator", "Failed to open camera : " + i);
                }
            };
            manager.openCamera(
                    Integer.toString(CameraCharacteristics.LENS_FACING_BACK),
                    stateCallback,
                    null
            );
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Create {@link ImageReader} and set listener on image available
     * @param imageAvailableListener Listener on image available
     */
    public void createImageReader(ImageReader.OnImageAvailableListener imageAvailableListener)
    {
        imageReader = ImageReader.newInstance(getPreviewSize().getWidth(), getPreviewSize().getHeight(),
                ImageFormat.NV21, 1);
        imageReader.setOnImageAvailableListener(imageAvailableListener, mainHandler);
    }
    private Size getPreviewSize()
    {
        return previewSize;
    }

    public void setPreviewSize(Size previewSize)
    {
        this.previewSize = previewSize;
    }

    /**
     * Get active camera
     * @return {@link CameraDevice}
     */
    public CameraDevice getCamera()
    {
        return camera;
    }

    public Handler getChildHandler()
    {
        return childHandler;
    }

    /**
     * Get {@link ImageReader} created by createImageReader
     * @return {@link ImageReader}
     */
    public ImageReader getImageReader()
    {
        return imageReader;
    }
}

/**
 * Manage sessions between camera and app
 */
class SessionManager
{
    private CameraCreator cameraCreator;
    private TextureView textureView;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession captureSession;

    /**
     * Create {@link SessionManager}
     * @param creator {@link CameraCreator} for camera data
     * @param textureView {@link TextureView} to show preview
     */
    SessionManager(final CameraCreator creator, TextureView textureView)
    {
        this.textureView = textureView;
        cameraCreator = creator;
    }

    /**
     * Create preview session
     */
    public void createPreview()
    {
        try
        {
            previewBuilder = cameraCreator.getCamera().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Surface surface = new Surface(textureView.getSurfaceTexture());
            previewBuilder.addTarget(surface);

            cameraCreator.getCamera().createCaptureSession(Arrays.asList(surface, cameraCreator.getImageReader().getSurface()), new CameraCaptureSession.StateCallback()
            {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession)
                {
                    captureSession = cameraCaptureSession;
                    try
                    {
                        previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        //                        previewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        CaptureRequest previewRequest = previewBuilder.build();
                        captureSession.setRepeatingRequest(previewRequest, null, cameraCreator.getChildHandler());
                    }
                    catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession)
                {
                    Log.e("SessionManager", "Config Failed");
                }
            }, cameraCreator.getChildHandler());
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Take picture from camera
     */
    public void takePicture()
    {
        CaptureRequest.Builder takePictureRequestBuilder;
        try
        {
            takePictureRequestBuilder = cameraCreator.getCamera().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            takePictureRequestBuilder.addTarget(cameraCreator.getImageReader().getSurface());
            takePictureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            takePictureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            CaptureRequest mCaptureRequest = takePictureRequestBuilder.build();
            captureSession.capture(mCaptureRequest, null, cameraCreator.getChildHandler());
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}