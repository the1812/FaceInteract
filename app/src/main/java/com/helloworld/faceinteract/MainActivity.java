package com.helloworld.faceinteract;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private final int PickPhotoRequest = 1;
    private FaceDataManager manager;
    private ImageView imageView;
    private PermissionHelper permissionHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = new FaceDataManager("/face-interact");
        imageView = (ImageView) findViewById(R.id.imageView);
        permissionHelper = new PermissionHelper(this);
        findViewById(R.id.buttonSelectPhoto).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                pickPhoto();
            }
        });
    }
    @Override
    protected void onDestroy()
    {
        if (manager != null)
        {
            manager.dispose();
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
                scanner.setManager(manager);
                imageView.setImageBitmap(scanner.getScannedBitmap());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
