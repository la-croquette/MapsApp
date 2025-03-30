package fr.imt_atlantique.mapsapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Activity to gather marker data:
 * - Title, Description, Optional Photo
 * - Photo saved as file, path returned to MainActivity
 */
public class AddMarkerActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_PICK = 200;

    private EditText editTitle, editDesc;
    private ImageView imagePreview;
    private Button btnTakePhoto, btnSelectImage, btnConfirm;

    private Bitmap currentBitmap;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);

        editTitle = findViewById(R.id.editTitle);
        editDesc = findViewById(R.id.editDesc);
        imagePreview = findViewById(R.id.imagePreview);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnConfirm = findViewById(R.id.btnConfirm);

        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        });

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        btnConfirm.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String desc = editDesc.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            String imagePath = null;
            if (currentBitmap != null) {
                imagePath = saveImageToFile(currentBitmap);
            }

            Intent result = new Intent();
            result.putExtra("latitude", latitude);
            result.putExtra("longitude", longitude);
            result.putExtra("title", title);
            result.putExtra("description", desc);
            result.putExtra("imagePath", imagePath);
            setResult(RESULT_OK, result);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                currentBitmap = (Bitmap) extras.get("data");
                imagePreview.setImageBitmap(currentBitmap);
            }
        } else if (requestCode == REQUEST_IMAGE_PICK) {
            Uri uri = data.getData();
            if (uri != null) {
                try (InputStream is = getContentResolver().openInputStream(uri)) {
                    currentBitmap = BitmapFactory.decodeStream(is);
                    imagePreview.setImageBitmap(currentBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String saveImageToFile(Bitmap bitmap) {
        String fileName = "marker_" + System.currentTimeMillis() + ".png";
        File file = new File(getFilesDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
