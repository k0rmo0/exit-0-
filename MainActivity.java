package com.soumio.inceptiontutorial;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    String lang = "bih";
    public static final int REQUEST_PERMISSION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},

                    REQUEST_PERMISSION);
        }

        Intent intent = getIntent();
        if(intent.getExtras() != null) {
            if(intent.getExtras().getInt("SKOK-MAPA") == 1) {
                Intent intent1 = new Intent(this, Map.class);
                startActivity(intent1);
            }
        }
    }

    public void lang(View view) {
        if(lang.equals("bih")) lang = "usa";
        else lang = "bih";
    }

    public void openGallery(View view) {
        Intent intent = new Intent(this, OpenGalleryActivity.class);
        intent.putExtra("lang", lang);
        startActivity(intent);
    }

    public void openCamera(View view) {
        Intent intent = new Intent(this, OpenCameraActivity.class);
        intent.putExtra("lang", lang);
        startActivity(intent);
    }
}

