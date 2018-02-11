package com.helloworld.faceinteract;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import java.util.Arrays;

public class CameraScanner
{
    private EngineManager engineManager;
    private SessionManager sessionManager;
    private CameraCreator cameraCreator;
    private ImageView imageView;
    private Bitmap bitmap;
    private ImageReader imageReader;

    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener()
    {
        @Override
        public void onImageAvailable(ImageReader imageReader)
        {

        }
    };
    public CameraScanner(Context context, final TextureView textureView, final ImageView imageView)
    {
        this.imageView = imageView;
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        cameraCreator = new CameraCreator(context, manager);
        cameraCreator.setPreviewSize(new Size(textureView.getMeasuredWidth(), textureView.getMeasuredHeight()));
        imageReader = cameraCreator.createImageReader(imageAvailableListener);
        sessionManager = new SessionManager(cameraCreator, textureView);
    }
    public void start()
    {
        sessionManager.createPreview();
    }
    public void takePicture()
    {
        sessionManager.takePicture();
    }
    public Bitmap getBitmap()
    {
        return bitmap;
    }
    public Bitmap getScannedBitmap()
    {
        PhotoScanner scanner = new PhotoScanner(bitmap);
        scanner.setEngineManager(engineManager);
        return scanner.getScannedBitmap();
    }
    public void setEngineManager(EngineManager engineManager)
    {
        this.engineManager = engineManager;
    }
}
class CameraCreator
{
    private Context context;
    private CameraManager manager;
    private HandlerThread thread;
    private Handler childHandler, mainHandler;
    private Size previewSize;
    private CameraDevice camera;
    private ImageReader imageReader;

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback()
    {
        @Override
        public void onOpened(CameraDevice cameraDevice)
        {
            camera = cameraDevice;
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice)
        {
            camera.close();
            camera = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i)
        {
            Log.e("CameraCreator", "Failed to open camera : " + i);
        }
    };
    public CameraCreator(Context context, CameraManager cameraManager)
    {
        assert manager != null;
        assert context != null;
        manager = cameraManager;
        this.context = context;

        thread = new HandlerThread("Camera2");
        thread.start();
        childHandler = new Handler(thread.getLooper());
        mainHandler = new Handler(context.getMainLooper());

        try
        {
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
    public ImageReader createImageReader(ImageReader.OnImageAvailableListener imageAvailableListener)
    {
        imageReader = ImageReader.newInstance(getPreviewSize().getWidth(), getPreviewSize().getHeight(),
                ImageFormat.NV21, 1);
        imageReader.setOnImageAvailableListener(imageAvailableListener, mainHandler);
        return imageReader;
    }
    public Size getPreviewSize()
    {
        return previewSize;
    }

    public void setPreviewSize(Size previewSize)
    {
        this.previewSize = previewSize;
    }

    public CameraDevice getCamera()
    {
        return camera;
    }

    public Handler getChildHandler()
    {
        return childHandler;
    }

    public ImageReader getImageReader()
    {
        return imageReader;
    }
}
class SessionManager
{
    private CameraCreator cameraCreator;
    private TextureView textureView;
    private CaptureRequest.Builder previewBuilder;
    private Surface surface;
    private CameraCaptureSession captureSession;

    public SessionManager(final CameraCreator creator, TextureView textureView)
    {
        this.textureView = textureView;
        cameraCreator = creator;
    }
    public void createPreview()
    {
        try
        {
            previewBuilder = cameraCreator.getCamera().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            surface = new Surface(textureView.getSurfaceTexture());
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
    public TextureView getTextureView()
    {
        return textureView;
    }
}