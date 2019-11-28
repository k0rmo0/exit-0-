package com.soumio.inceptiontutorial;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Classify extends AppCompatActivity {

    private boolean isplayed = false;

    private MediaPlayer tvrtkoAudioen, tvrtkoAudiobs, barokAudioen, barokAudiobs, crkvaAudioen, crkvaAudiobs, fontanaAudioen,
            fontanaAudiobs, ismetmesaAudioen, ismetmesaAudiobs, kapijaAudioen, kapijaAudiobs, zenaAudioen, zenaAudiobs;

    private static final int RESULTS_TO_SHOW = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    private final Interpreter.Options tfliteOptions;
    private Interpreter tflite;
    private List<String> labelList;
    private ByteBuffer imgData = null;
    private float[][] labelProbArray = null;
    private byte[][] labelProbArrayB = null;
    private String[] topLables = null;
    private String[] topConfidence = null;

    private String chosen;
    private boolean quant;

    private int DIM_IMG_SIZE_X = 224;
    private int DIM_IMG_SIZE_Y = 224;
    private int DIM_PIXEL_SIZE = 3;

    private int[] intValues;

    private ImageView selected_image;
    private String label1;
    private String label2;
    private String label3;
    private String Confidence1;
    private String Confidence2;
    private String Confidence3;

    private String lang;

    TextView titleTv, descriptionTv;
    ImageView imageView;
    DatabaseReference reffTitle, reffImage, reffDescription;

    private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });

    public Classify() {
        tfliteOptions = new Interpreter.Options();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tvrtkoAudioen = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);
        tvrtkoAudiobs = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkobs);

        barokAudioen = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);
        barokAudiobs = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);

        crkvaAudioen = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);
        crkvaAudiobs = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);

        fontanaAudioen = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);
        fontanaAudiobs = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);

        ismetmesaAudioen = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);
        ismetmesaAudiobs = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);

        kapijaAudioen = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);
        kapijaAudiobs = MediaPlayer.create(getApplicationContext(), R.raw.tvrtkoen);

        zenaAudioen = MediaPlayer.create(getApplicationContext(), R.raw.zenakipen);
        zenaAudiobs = MediaPlayer.create(getApplicationContext(), R.raw.zenakipbs);

        chosen = (String) getIntent().getStringExtra("chosen");
        quant = (boolean) getIntent().getBooleanExtra("quant", false);

        intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        try{
            tflite = new Interpreter(loadModelFile(), tfliteOptions);
            labelList = loadLabelList();
        } catch (Exception ex){
            ex.printStackTrace();
        }

        if(quant){
            imgData =
                    ByteBuffer.allocateDirect(
                            DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        } else {
            imgData =
                    ByteBuffer.allocateDirect(
                            4 * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        }
        imgData.order(ByteOrder.nativeOrder());

        if(quant){
            labelProbArrayB= new byte[1][labelList.size()];
        } else {
            labelProbArray = new float[1][labelList.size()];
        }

        setContentView(R.layout.activity_classify);

        if(getIntent().getExtras() != null) lang = getIntent().getExtras().getString("lang");

        selected_image = (ImageView) findViewById(R.id.Slika);

        topLables = new String[RESULTS_TO_SHOW];
        topConfidence = new String[RESULTS_TO_SHOW];

        Uri uri = (Uri)getIntent().getParcelableExtra("resID_uri");
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            selected_image.setImageBitmap(bitmap);
            selected_image.setRotation(selected_image.getRotation() + 90);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap_orig = ((BitmapDrawable)selected_image.getDrawable()).getBitmap();

        int width  = bitmap_orig.getWidth();
        int height = bitmap_orig.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap_orig, cropW, cropH, newWidth, newHeight);
        Bitmap bitmap = getResizedBitmap(cropImg, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y);
        convertBitmapToByteBuffer(bitmap);
        if(quant){
            tflite.run(imgData, labelProbArrayB);
        } else {
            tflite.run(imgData, labelProbArray);
        }

        String nr =  new String("Not recognizable!");
        String np = new String("Ne prepoznajem!");

        printTopKLabels();
        titleTv = (TextView) findViewById(R.id.titleTv);
        if(topConfidence[2].charAt(0) == '8' || topConfidence[2].charAt(0) == '9')
            searchPrintFromBase(label1);
        else if(lang.equals("usa")) titleTv.setText(nr);
        else titleTv.setText(np);
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(chosen);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                if(quant){
                    imgData.put((byte) ((val >> 16) & 0xFF));
                    imgData.put((byte) ((val >> 8) & 0xFF));
                    imgData.put((byte) (val & 0xFF));
                } else {
                    imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                }

            }
        }
    }

    private List<String> loadLabelList() throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(this.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private void printTopKLabels() {
        for (int i = 0; i < labelList.size(); ++i) {
            if(quant){
                sortedLabels.add(
                        new AbstractMap.SimpleEntry<>(labelList.get(i), (labelProbArrayB[0][i] & 0xff) / 255.0f));
            } else {
                sortedLabels.add(
                        new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
            }
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }

        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            topLables[i] = label.getKey();
            topConfidence[i] = String.format("%.0f%%",label.getValue()*100);
        }

        label1 = topLables[2];
        label2 = topLables[1];
        label3 = topLables[0];

        /*
        TextView naziv = findViewById(R.id.Naziv);
        naziv.setText(label1);

         */

        Confidence1 = topConfidence[2];
        Confidence2 = topConfidence[1];
        Confidence3 = topConfidence[0];
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public void searchPrintFromBase(String label){

        imageView = (ImageView) findViewById(R.id.imageView);
        descriptionTv = (TextView) findViewById(R.id.descriptionTv);
        descriptionTv.setMovementMethod(new ScrollingMovementMethod());

        reffTitle = FirebaseDatabase.getInstance().getReference().child(label).child("title" + "_" + lang);
        reffTitle.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String titleStr = dataSnapshot.getValue().toString();
                    titleTv.setText(titleStr);
                }
                else {
                    titleTv.setText("No data found!");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                titleTv.setText("Greska pri ucitavanju iz baze podataka");
            }
        });

        reffImage = FirebaseDatabase.getInstance().getReference().child(label).child("image");
        reffImage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String imageUrl = dataSnapshot.getValue().toString();
                    Picasso.get().load(imageUrl).into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String errorUrl = "https://firebasestorage.googleapis.com/v0/b/bazazatourist.appspot.com/o/error.png?alt=media&token=be556a4b-74da-403e-8b9f-430827731b9c";
                Picasso.get().load(errorUrl).into(imageView);
            }
        });

        reffDescription = FirebaseDatabase.getInstance().getReference().child(label).child("description" + "_" + lang);
        reffDescription.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String descriptionStr = dataSnapshot.getValue().toString();
                    descriptionTv.setText(descriptionStr);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                descriptionTv.setText("Greska pri ucitavanju iz baze podataka");
            }
        });


    }

    public void goHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void playButton(View view) {

        if(isplayed) {
            if(lang.equals("usa")) {
                if(label1.equals("tvrtko")) tvrtkoAudioen.stop();
                else if(label1.equals("barok")) barokAudioen.stop();
                else if(label1.equals("ismet_mesa")) ismetmesaAudioen.stop();
                else if(label1.equals("zena")) zenaAudioen.stop();
                else if(label1.equals("crkva")) crkvaAudioen.stop();
                else if(label1.equals("fontana")) fontanaAudioen.stop();
                else if(label1.equals("kapija")) kapijaAudioen.stop();
            }
            else {
                if(label1.equals("tvrtko")) tvrtkoAudiobs.stop();
                else if(label1.equals("barok")) barokAudiobs.stop();
                else if(label1.equals("ismet_mesa")) ismetmesaAudiobs.stop();
                else if(label1.equals("zena")) zenaAudiobs.stop();
                else if(label1.equals("crkva")) crkvaAudiobs.stop();
                else if(label1.equals("fontana")) fontanaAudiobs.stop();
                else if(label1.equals("kapija")) kapijaAudiobs.stop();
            }
        }

        isplayed = true;

        if(lang.equals("usa")) {
            if(label1.equals("tvrtko")) tvrtkoAudioen.start();
            else if(label1.equals("barok")) barokAudioen.start();
            else if(label1.equals("ismet_mesa")) ismetmesaAudioen.start();
            else if(label1.equals("zena")) zenaAudioen.start();
            else if(label1.equals("crkva")) crkvaAudioen.start();
            else if(label1.equals("fontana")) fontanaAudioen.start();
            else if(label1.equals("kapija")) kapijaAudioen.start();
        }
        else {
            if(label1.equals("tvrtko")) tvrtkoAudiobs.start();
            else if(label1.equals("barok")) barokAudiobs.start();
            else if(label1.equals("ismet_mesa")) ismetmesaAudiobs.start();
            else if(label1.equals("zena")) zenaAudiobs.start();
            else if(label1.equals("crkva")) crkvaAudiobs.start();
            else if(label1.equals("fontana")) fontanaAudiobs.start();
            else if(label1.equals("kapija")) kapijaAudiobs.start();
        }
    }

    public void openMap(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("SKOK-MAPA",1);
        startActivity(intent);
        finish();
    }
}