package com.helloworld.faceinteract;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.*;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private final int PickPhotoRequest = 1;
    private FaceDataManager faceDataManager;
    private EngineManager engineManager;
    private ImageView imageView;
    private TextureView textureView;
    private TextView textView;
    private Button buttonSaveFace;
    private PermissionHelper permissionHelper;
    private Face currentFace;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        faceDataManager = new FaceDataManager("/face-interact");
        engineManager = new EngineManager();
        imageView = (ImageView) findViewById(R.id.imageView);
        textureView = (TextureView) findViewById(R.id.textureView);
        textView = (TextView) findViewById(R.id.textView);
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
        buttonSaveFace = (Button) findViewById(R.id.buttonSaveFace);
        buttonSaveFace.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showInputNameDialog();
            }
        });
        buttonSaveFace.setVisibility(View.GONE);
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
                cameraScanner.setFaceDataManager(faceDataManager);
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
                        loadFace(cameraScanner.extractFace());
                    }
                });
            }
        });
    }
    private void showInputNameDialog()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        LinearLayout layout= new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50,15,50,15);

        alert.setTitle(R.string.save_face);

        final TextView nameText = new TextView(this);
        nameText.setText(R.string.name);
        final EditText nameInput = new EditText(this);

        layout.addView(nameText);
        layout.addView(nameInput);

        alert.setView(layout);
        alert.setPositiveButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                    }
                });
        alert.setNegativeButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        String name = nameInput.getText().toString();
                        currentFace.setName(name);
                        saveFace();
                    }
                });
        alert.show();
    }
    private void saveFace()
    {
        if (currentFace == null || currentFace.getName() == null)
        {
            return;
        }
        faceDataManager.saveFace(currentFace);
    }
    private void loadFace(Face newFace)
    {
        currentFace = newFace;
        if (currentFace != null)
        {
            buttonSaveFace.setVisibility(View.GONE);
            textView.setText(currentFace.getName());
        }
        else
        {
            buttonSaveFace.setVisibility(View.VISIBLE);
        }
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
                scanner.setFaceDataManager(faceDataManager);
                scanner.setEngineManager(engineManager);
                imageView.setImageBitmap(scanner.getScannedBitmap());
                loadFace(scanner.extractFace());
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
