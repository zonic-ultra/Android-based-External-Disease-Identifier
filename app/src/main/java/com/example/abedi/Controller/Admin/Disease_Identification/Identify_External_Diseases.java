
package com.example.abedi.Controller.Admin.Disease_Identification;

import static java.lang.Character.toUpperCase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abedi.Model.TreatmentSuggestionModel;
import com.example.abedi.R;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Identify_External_Diseases extends AppCompatActivity {
    // Constants to define the file suffix for model and labels
    private static final String model_file_suffix = "_model.tflite";
    private static final String labels_file_suffix = "_labels.txt";
    // TensorFlow Lite interpreter for loading and running the model
    private Interpreter tflite;
    // List to store labels for the model predictions
    private List<String> labels = new ArrayList<>();

    private TextView resultTextView;
    private ImageView imageView;
    private Bitmap selectedImage;
    private int image_size = 224;
    private String currentAnimal = "dog"; // default animal

    // Request codes for opening the camera and accessing files
    private static final int camera_request_code = 3;
    private static final int camera_permission_request_code = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_identify_external_diseases);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button uploadModelButton = findViewById(R.id.uploadModelButton);
        Button uploadLabelButton = findViewById(R.id.uploadLabelsButton);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button openCameraButton = findViewById(R.id.openCameraButton);
        Button predictButton = findViewById(R.id.predictButton);
        imageView = findViewById(R.id.imageView);

        resultTextView = findViewById(R.id.resultTextView);

        TextView textView = findViewById(R.id.resultTextView);
        textView.setMovementMethod(new ScrollingMovementMethod());



        // List of animals for selection in the spinner
        Spinner animalSelector = findViewById(R.id.animalSelector);
        List<String> animals = new ArrayList<>();
        animals.add("Dog");
        animals.add("Cat");
        animals.add("Rabbit");
        animals.add("Swine");
        animals.add("Goat");
        animals.add("Cow");

        // Set up the spinner to allow selection of animals
        ArrayAdapter<String> animalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, animals);
        animalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        animalSelector.setAdapter(animalAdapter);

        // Set listener to update the selected animal and load its model and labels
        animalSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currentAnimal = parentView.getItemAtPosition(position).toString().toLowerCase();
                loadModelAndLabels(); // Load the model and labels for the selected animal
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if no animal is selected
            }
        });

        // Load the initial model and labels for the default animal (dog)
        loadModelAndLabels();

        // Set button click listeners to trigger actions
        uploadModelButton.setOnClickListener(v -> openFilePicker("model"));
        uploadLabelButton.setOnClickListener(v -> openFilePicker("labels"));
        selectImageButton.setOnClickListener(v -> openGallery());
        openCameraButton.setOnClickListener(v -> checkCameraPermissionAndOpen());
        predictButton.setOnClickListener(v -> performPrediction());

    }

    // Method to open a file picker to select either model or labels
    private void openFilePicker(String type) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (type.equals("model")) {
            intent.setType("application/octet-stream"); // Select model file
            startActivityForResult(Intent.createChooser(intent, "Select Model File"), 100);
        } else if (type.equals("labels")) {
            intent.setType("text/plain"); // Select labels file
            startActivityForResult(Intent.createChooser(intent, "Select Labels File"), 101);
        }
    }

    // Check if the app has permission to use the camera, otherwise ask for permission
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera(); // If permission is granted, open the camera
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, camera_permission_request_code);
        }
    }

    // Method to open the camera and capture an image
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, camera_request_code); // Start camera activity
        }
    }

    // Method to open the gallery and select an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1); // Start gallery activity
    }

    // This method handles the result of any activity (like selecting a file or taking a picture)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData(); // Get the file URI

            if (requestCode == 100) { // Model file
                saveFileToInternalStorage(fileUri, currentAnimal + model_file_suffix);
                loadModelAndLabels(); // Reload model and labels after upload
            } else if (requestCode == 101) { // Labels file
                saveFileToInternalStorage(fileUri, currentAnimal + labels_file_suffix);
                loadLabelsFromInternalStorage(); // Load labels after upload
            } else if (requestCode == 1) { // Gallery image
                try {
                    selectedImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
                    imageView.setImageBitmap(selectedImage); // Display the selected image
                } catch (IOException e) {
                    e.printStackTrace(); // Handle error if image loading fails
                }
            } else if (requestCode == camera_request_code) { // Camera image
                if (data != null && data.getExtras() != null) {
                    selectedImage = (Bitmap) data.getExtras().get("data");
                    imageView.setImageBitmap(selectedImage); // Display the captured image
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Method to save the selected file to internal storage
    private void saveFileToInternalStorage(Uri fileUri, String fileName) {
        try (InputStream inputStream = getContentResolver().openInputStream(fileUri);
             FileOutputStream outputStream = openFileOutput(fileName, MODE_PRIVATE)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length); // Write the file to internal storage
            }
            Toast.makeText(this, fileName + " saved successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle error while saving file
            Toast.makeText(this, "Failed to save " + fileName, Toast.LENGTH_SHORT).show();
        }
    }

    // Method to load the model and labels from internal storage based on the selected animal
    private void loadModelAndLabels() {
        loadModelFromInternalStorage();
        loadLabelsFromInternalStorage(); // Load both model and labels for the animal
    }

    // Method to load the model from internal storage
    private void loadModelFromInternalStorage() {
        String modelFileName = currentAnimal + model_file_suffix;
        try (FileInputStream inputStream = openFileInput(modelFileName)) {
            ByteBuffer modelBuffer = ByteBuffer.allocateDirect(inputStream.available());
            modelBuffer.order(ByteOrder.nativeOrder()); // Set the byte order for the model

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                modelBuffer.put(buffer, 0, length); // Read the model into buffer
            }
            modelBuffer.rewind();

            tflite = new Interpreter(modelBuffer); // Load the TensorFlow Lite model
            Toast.makeText(this, "Model for " + currentAnimal + " loaded!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle error if model loading fails
            Toast.makeText(this, "No model found for " + currentAnimal + ". Please upload a model.", Toast.LENGTH_SHORT).show();
            tflite = null;  // Set model to null if not found
        }
        updatePredictButtonState();
    }

    // Method to load labels from internal storage
    private void loadLabelsFromInternalStorage() {
        String labelsFileName = currentAnimal + labels_file_suffix;
        try (FileInputStream inputStream = openFileInput(labelsFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            labels.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line); // Add labels to the list
            }
            Toast.makeText(this, "Labels for " + currentAnimal + " loaded!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle error if labels loading fails
            Toast.makeText(this, "No labels found for " + currentAnimal + ". Please upload labels.", Toast.LENGTH_SHORT).show();
        }
        updatePredictButtonState();
    }

    // Method to enable or disable the predict button based on model and labels availability
    private void updatePredictButtonState() {
        Button predictButton = findViewById(R.id.predictButton);

        if (tflite != null && !labels.isEmpty()) {
            predictButton.setEnabled(true); // Enable button if model and labels are loaded
        } else {
            predictButton.setEnabled(false); // Disable button if model or labels are missing
        }
    }

    // Method to make a prediction based on the selected image
    private void performPrediction() {
//        if (selectedImage == null) {
//            Toast.makeText(this, "Please select or capture an image first!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (tflite == null || labels.isEmpty()) {
//            Toast.makeText(this, "Model or labels are not loaded!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Bitmap resizedImage = Bitmap.createScaledBitmap(selectedImage, image_size, image_size, true); // Resize image
//        ByteBuffer inputBuffer = preprocessImage(resizedImage); // Prepare image for prediction
//
//        float[][] output = new float[1][labels.size()];
//        tflite.run(inputBuffer, output); // Run the model to get predictions
//
//        int maxIndex = getMaxConfidenceIndex(output[0]); // Get the index of the highest confidence
//
//        if (maxIndex > 8.0) {
//            resultTextView.setText("Result: Unknown"); // Show if no valid prediction is made
//        } else {
//            String result = "Result: " + labels.get(maxIndex); // Show the predicted label
//            resultTextView.setText(result);
//        }
        if (selectedImage == null) {
            Toast.makeText(this, "Please select or capture an image first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tflite == null || labels.isEmpty()) {
            Toast.makeText(this, "Model or labels are not loaded!", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap resizedImage = Bitmap.createScaledBitmap(selectedImage, image_size, image_size, true);
        ByteBuffer inputBuffer = preprocessImage(resizedImage);

        float[][] output = new float[1][labels.size()];
        tflite.run(inputBuffer, output);

        int maxIndex = getMaxConfidenceIndex(output[0]); // Get index of highest confidence prediction

        if (maxIndex >= labels.size()) {
            resultTextView.setText("Result: Unknown Disease");
            return;
        }

        String predictedDisease = labels.get(maxIndex).trim();

        // Create instance of TreatmentSuggestionModel
        TreatmentSuggestionModel treatmentModel = new TreatmentSuggestionModel(this);

        // Fetch treatment from database
        String treatment = treatmentModel.getTreatmentFromDatabase(predictedDisease);

        if (treatment.equals("No treatment information available")) {
            resultTextView.setText(predictedDisease + "\n\nNo treatment found in database.");
        } else {
            resultTextView.setText(predictedDisease + "\n\nSuggested Treatment:\n" + treatment);
        }
    }

    // Method to preprocess the image for model input
    private ByteBuffer preprocessImage(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * image_size * image_size * 3); // Create buffer for the image
        buffer.order(ByteOrder.nativeOrder()); // Set the byte order

        int[] pixels = new int[image_size * image_size]; // Get the pixels of the image
        bitmap.getPixels(pixels, 0, image_size, 0, 0, image_size, image_size);

        // Normalize each pixel and put it into the buffer
        for (int pixel : pixels) {
            buffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f);
            buffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);
            buffer.putFloat((pixel & 0xFF) / 255.0f);
        }

        return buffer; // Return the processed image buffer
    }

    // Method to find the index of the label with the highest confidence
    private int getMaxConfidenceIndex(float[] confidences) {
        int maxIndex = -1;
        float maxConfidence = -1;
        for (int i = 0; i < confidences.length; i++) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i];
                maxIndex = i;
            }
        }
        return maxIndex; // Return the index of the label with the highest confidence
    }
}
