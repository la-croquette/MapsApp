package fr.imt_atlantique.mapsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Displays marker's details: title, description, image
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
        String base64 = getIntent().getStringExtra("base64");

        txtTitle.setText(title);
        txtDesc.setText(desc);

        if (base64 != null && !base64.isEmpty()) {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imgMarker.setImageBitmap(bmp);
        }
    }
}
