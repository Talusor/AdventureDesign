package kr.cnu.ai.lth.adventuredesign;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class SettingActivity extends AppCompatActivity {
    ImageView closeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        closeView = findViewById(R.id.closeButton);
        closeView.setOnClickListener(v -> finish());
    }
}