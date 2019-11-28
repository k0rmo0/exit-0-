package com.soumio.inceptiontutorial;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class OpenCameraActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE = 100;
    ImageView imageView;
    private Uri imageUri;
    ContentValues values = new ContentValues();
    private String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        if(getIntent().getExtras() != null) lang = getIntent().getExtras().getString("lang");

        imageView = findViewById(R.id.image);
        openCameraIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            try {
                Intent intent = new Intent(this, Classify.class);
                intent.putExtra("lang", lang);
                intent.putExtra("resID_uri", imageUri);
                String chosen = "inception_quant.tflite";
                intent.putExtra("chosen", chosen);
                boolean quant = true;
                intent.putExtra("quant", quant);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openCameraIntent(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        startActivityForResult(intent, REQUEST_IMAGE);
    }
}
