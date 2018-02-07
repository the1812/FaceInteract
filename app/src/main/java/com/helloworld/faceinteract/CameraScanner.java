package com.helloworld.faceinteract;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.TextureView;

public class CameraScanner
{
    private CameraManager manager;
    private CameraDevice camera;
    private TextureView textureView;
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback()
    {
        @Override
        public void onOpened(CameraDevice cameraDevice)
        {
            camera = cameraDevice;
            createPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice)
        {

        }

        @Override
        public void onError(CameraDevice cameraDevice, int i)
        {

        }
    };

    private void createPreview()
    {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        assert texture != null;
        Log.e("Error", "Incomplete method");
    }

    public CameraScanner(Context context, TextureView textureView)
    {
        this.textureView = textureView;
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        assert manager != null;
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

}
