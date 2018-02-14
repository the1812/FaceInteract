package com.helloworld.faceinteract;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private final int PickPhotoRequest = 1;
    private FaceDataManager dataManager;
    private EngineManager engineManager;
    private ImageView imageView;
    private TextureView textureView;
    private PermissionHelper permissionHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataManager = new FaceDataManager("/face-interact");
        engineManager = new EngineManager();
        imageView = (ImageView) findViewById(R.id.imageView);
        textureView = (TextureView) findViewById(R.id.textureView);
        permissionHelper = new PermissionHelper(this);
        findViewById(R.id.buttonSelectPhoto).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                imageView.setVisibility(View.VISIBLE);
                textureView.setVisibility(View.GONE);
                pickPhoto();
            }
        });
        findViewById(R.id.buttonOpenCamera).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                imageView.setVisibility(View.GONE);
                textureView.setVisibility(View.VISIBLE);
                openCamera();
            }
        });
    }
    @Override
    protected void onDestroy()
    {
        if (engineManager != null)
        {
            engineManager.dispose();
        }
        super.onDestroy();
    }
    private void pickPhoto()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_photo)), PickPhotoRequest);
    }
    private void openCamera()
    {
        permissionHelper.requestPermission(Manifest.permission.CAMERA, new Action()
        {
            @Override
            public void invoke()
            {
                final CameraScanner cameraScanner = new CameraScanner(MainActivity.this, textureView);
                cameraScanner.setEngineManager(engineManager);
                cameraScanner.start();
                textureView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        cameraScanner.takePicture();
                        imageView.setImageBitmap(cameraScanner.getScannedBitmap());
                        imageView.setVisibility(View.VISIBLE);
                        textureView.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Result",""+requestCode);
        if (requestCode == PickPhotoRequest &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null)
        {
            Uri uri = data.getData();
            Log.d("Uri", uri.toString());
            try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                PhotoScanner scanner = new PhotoScanner(bitmap);
                scanner.setFaceDataManager(dataManager);
                scanner.setEngineManager(engineManager);
                imageView.setImageBitmap(scanner.getScannedBitmap());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults)
    {
        permissionHelper.onRequestPermissionsResult(requestCode, grantResults);
    }
}
