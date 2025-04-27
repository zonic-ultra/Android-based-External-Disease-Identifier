package com.example.abedi.Controller.Admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abedi.R;
import com.example.abedi.ml.DogExternalDiseasesModel;
import com.example.abedi.ml.MlModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IdentifyDisease extends AppCompatActivity {
    Button camera, gallery;
    ImageView imageView;
    TextView result;
    int imageSize = 224;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_identify_disease);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if(Build.VERSION.SDK_INT>=19){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        camera = findViewById(R.id.camera_btn);
        gallery = findViewById(R.id.gallery_btn);
        imageView = findViewById(R.id.imageView);
        result = findViewById(R.id.result);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    // Check for camera permission before launching camera
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });
    }

    // Method to identify the disease from the selected image
    public void identifyImage(Bitmap image){
        try {
            //Load the model
            MlModel model = MlModel.newInstance(getApplicationContext());

            // Create a buffer to hold the image data in the correct format
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            // Preprocess the image by converting it to the correct format and scaling
            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;

            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageSize; i ++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            MlModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();

            // Find the indexes of the top 2 confidence values
            int firstMaxPos = 0, secondMaxPos = 0;
            float firstMaxConfidence = 0, secondMaxConfidence = 0;

            // Process the confidences to find the top predictions
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > firstMaxConfidence) {

                    // Shift the first to second before updating
                    secondMaxConfidence = firstMaxConfidence;
                    secondMaxPos = firstMaxPos;

                    firstMaxConfidence = confidences[i];
                    firstMaxPos = i;
                } else if (confidences[i] > secondMaxConfidence) {
                    secondMaxConfidence = confidences[i];
                    secondMaxPos = i;
                }
            }

            String[] classes = {"Cataract Eye", "Cherry Eye", "Conjunctivitis", "Glaucoma Eye", "Iris Atrophy Eye"};

            if (firstMaxConfidence < 0.8) {
//                result.setText("The input does not appear to match any known diseases with high confidence.");
                result.setText("unknown");
            } else {
                // Display the top 2 predictions
                String top2Predictions = "Top 2 predictions" + "\n\n1. " + classes[firstMaxPos] + " - " + String.format("%.2f", firstMaxConfidence * 100) + "% Confidence";

                // If the second prediction has a good confidence
                if (secondMaxConfidence >= 0.2) {
                    top2Predictions += "\n2. " + classes[secondMaxPos] + " - " + String.format("%.2f", secondMaxConfidence * 100) + "% Confidence";
                } else {
                    top2Predictions += "\n2. The second prediction has low confidence and may not match any known disease";
                }

                result.setText(top2Predictions);
            }

            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == 3) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                // Resize the image to be square
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

                imageView.setImageBitmap(image);

                // Scale the image to the required input size and classify it
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                identifyImage(image);

            } else {
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.getStackTrace();
                }
                imageView.setImageBitmap(image);

                // Scale the image to the required input size and classify it
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                identifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}