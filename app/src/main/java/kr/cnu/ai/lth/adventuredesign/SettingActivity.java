package kr.cnu.ai.lth.adventuredesign;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {
    ImageView closeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        closeView = findViewById(R.id.closeButton);
        closeView.setOnClickListener(v -> finish());

        Settings settings = Manager.getInstance().getSettings();

        TextView alarmText = findViewById(R.id.alarmSettingText);
        TextView alarmVolText = findViewById(R.id.alarmVolumeText);
        TextView ventMsgText = findViewById(R.id.ventMessageText);
        TextView ventText = findViewById(R.id.ventSettingText);

        alarmText.setText(getResources().getResourceName(settings.getAlarmId()));
        alarmVolText.setText(String.valueOf(settings.getAlarmVolume()));
        ventMsgText.setText(settings.getVentMsg());
        ventText.setText(settings.getVentType().name());
    }
}