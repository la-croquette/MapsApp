package fr.imt_atlantique.mapsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * Displays marker's details: title, description, image (loaded from path)
 */
public class MarkerDetailActivity extends AppCompatActivity {

    private TextView txtTitle, txtDesc;
    private ImageView imgMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_detail);

        txtTitle = findViewById(R.id.txtTitle);
        txtDesc = findViewById(R.id.txtDesc);
        imgMarker = findViewById(R.id.imgMarker);

        // Get data from intent
        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("description");
        String imagePath = getIntent().getStringExtra("imagePath");

        txtTitle.setText(title != null ? title : "(No Title)");
        txtDesc.setText(desc != null ? desc : "(No Description)");

        // Load and show original image from file
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(imagePath);
                imgMarker.setImageBitmap(bmp);
            }
        }
    }
}
