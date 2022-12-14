package kr.cnu.ai.lth.adventuredesign;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {
    ImageView closeView;

    @SuppressLint("DefaultLocale")
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
        TextView naviText = findViewById(R.id.naviSettingText);
        TextView shelterText = findViewById(R.id.shelterSettingText);

        //alarmText.setText(getResources().getResourceName(settings.getAlarmId()));
        alarmVolText.setText(String.valueOf(settings.getAlarmVolume()));
        ventMsgText.setText(settings.getVentMsg());
        ventText.setText(settings.getVentType().name());
        naviText.setText(settings.getNaviType().name());
        shelterText.setText(String.valueOf(settings.getShelterLimit()));

        findViewById(R.id.naviSetting).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("네비케이션 어플 선택");

            builder.setItems(R.array.navi_list, (d, pos) -> {
                String item = getResources().getStringArray(R.array.navi_list)[pos];
                if (item.equals("네이버맵"))
                    settings.setNaviType(NaviType.NAVER);
                if (item.equals("카카오맵"))
                    settings.setNaviType(NaviType.KAKAO);
                if (item.equals("TMAP"))
                    settings.setNaviType(NaviType.TMAP);
                if (Manager.getInstance().checkNavi(this))
                    Toast.makeText(getApplicationContext(), String.format("네이버게이션 어플 (%s) 설정", item), Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(getApplicationContext(), "해당 어플리케이션이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                    settings.setNaviType(NaviType.NONE);
                }

                naviText.setText(settings.getNaviType().name());
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        findViewById(R.id.alarmVolume).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View view = this.getLayoutInflater().inflate(R.layout.dialog_number_picker, null);
            builder.setView(view);
            builder.setTitle("볼륨을 설정해주세요.");
            final NumberPicker picker = (NumberPicker) view.findViewById(R.id.picker);
            picker.setMinValue(10);
            picker.setMaxValue(100);
            picker.setValue(settings.getAlarmVolume());
            builder.setPositiveButton("확인", (d, id) -> {
                        Toast.makeText(this, String.format("볼륨을 %d으로 설정하였습니다.", picker.getValue()), Toast.LENGTH_SHORT).show();
                        settings.setAlarmVolume(picker.getValue());

                        alarmVolText.setText(String.valueOf(settings.getAlarmVolume()));
                    })
                    .setNegativeButton("취소", null);
            builder.create().show();
        });

        findViewById(R.id.shelterSetting).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View view = this.getLayoutInflater().inflate(R.layout.dialog_number_picker, null);
            builder.setView(view);
            builder.setTitle("표시 개수를 설정해주세요.");
            final NumberPicker picker = (NumberPicker) view.findViewById(R.id.picker);
            picker.setMinValue(5);
            picker.setMaxValue(50);
            picker.setValue(settings.getShelterLimit());
            builder.setPositiveButton("확인", (d, id) -> {
                        Toast.makeText(this, String.format("표시 개수를 %d개로 설정하였습니다.", picker.getValue()), Toast.LENGTH_SHORT).show();
                        settings.setShelterLimit(picker.getValue());

                        shelterText.setText(String.valueOf(settings.getShelterLimit()));
                    })
                    .setNegativeButton("취소", null);
            builder.create().show();
        });

        findViewById(R.id.ventSetting).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("환기 알림 선택");

            builder.setItems(R.array.ventType_list, (d, pos) -> {
                String item = getResources().getStringArray(R.array.ventType_list)[pos];
                if (item.equals("알림 없음"))
                    settings.setVentType(VentType.NO_SOUND);
                if (item.equals("음성 알림"))
                    settings.setVentType(VentType.WITH_TTS);

                ventText.setText(settings.getVentType().name());
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    @Override
    protected void onStop() {
        Manager manager = Manager.getInstance();
        Settings settings = manager.getSettings();
        super.onStop();
        SharedPreferences perf = getSharedPreferences("setting", MODE_PRIVATE);
        SharedPreferences.Editor editor = perf.edit();
        editor.putString("ventType", settings.getVentType().name());
        editor.putString("naviType", settings.getNaviType().name());
        editor.putInt("vol", settings.getAlarmVolume());
        editor.putInt("shelterLimit", settings.getShelterLimit());
        editor.apply();
    }
}