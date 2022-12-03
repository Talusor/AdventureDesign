package kr.cnu.ai.lth.adventuredesign;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;
import java.util.Random;

import kr.cnu.ai.lth.adventuredesign.History.HistoryFragment;
import kr.cnu.ai.lth.adventuredesign.Shelter.ShelterFragment;

public class MainActivity extends AppCompatActivity {
    Manager manager = Manager.getInstance();
    Random rnd = new Random();

    FragmentManager fm;
    ShelterFragment shelterFragment;
    HistoryFragment historyFragment;

    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        try {
            manager.LoadDB(this);
        } catch (Exception e) {
            Log.e(manager.TAG, e.getMessage());
        }

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        toolbar.setNavigationOnClickListener(v -> {

        });

        fm = getSupportFragmentManager();
        shelterFragment = new ShelterFragment();
        historyFragment = new HistoryFragment();
        ChangeView(0);

        LinearLayout shelterButton = findViewById(R.id.shelterButton);
        LinearLayout historyButton = findViewById(R.id.historyButton);
        startButton = findViewById(R.id.startButton);
        shelterButton.setOnClickListener(v -> btnClick());
        historyButton.setOnClickListener(v -> btnClick2());
        startButton.setOnClickListener(v -> btnClick3());
        updateButton();
    }

    private void updateButton() {
        if (manager.isDriveServiceRunning(this, DuringDrive.class)) {
            startButton.setText("운전 종료");
        } else {
            startButton.setText("운전 시작");
        }
    }

    private void checkPermission(Runnable r) {
        TedPermission.create()
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        r.run();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(getApplicationContext(), "권한 거부됨.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setDeniedMessage("권한 거부됨.")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    public void btnClick() {
        ChangeView(0);
        checkPermission(() -> {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) {
                double lat = loc.getLatitude();
                double lng = loc.getLongitude();
                shelterFragment.RefreshShelters(lat, lng, 10);
            }
        });
    }

    public void btnClick2() {
        ChangeView(1);
        historyFragment.Test();
    }

    public void btnClick3() {
        //manager.insertHistory(rnd.nextInt(10) + 1, rnd.nextInt(265) + 20);
        Intent intent = new Intent(this, DuringDrive.class);
        if (startButton.getText().toString().equals("운전 종료")) {
            intent.setAction("STOP");
            startForegroundService(intent);

            new Thread(() -> {
                try {
                    while (manager.isDriveServiceRunning(this, DuringDrive.class)) {
                        Thread.sleep(100);
                    }
                } catch (Exception ignored) {
                }
                Log.d(manager.TAG, "Started Date : " + manager.getStartDate().toString());
                Log.d(manager.TAG, "End Date : " + manager.getEndDate().toString());
            }).start();
        } else {
            startForegroundService(intent);
        }
        new Handler().postDelayed(this::updateButton, 100);
    }

    private void ChangeView(int ID) {
        FragmentTransaction ft = fm.beginTransaction();

        switch (ID) {
            case 1:
                ft.replace(R.id.frameView, historyFragment).commitNowAllowingStateLoss();
                break;
            default:
                ft.replace(R.id.frameView, shelterFragment).commitNowAllowingStateLoss();
                break;
        }
    }
}