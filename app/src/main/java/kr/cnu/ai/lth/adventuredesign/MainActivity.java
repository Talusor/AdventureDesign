package kr.cnu.ai.lth.adventuredesign;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;
import java.util.Map;

import kr.cnu.ai.lth.adventuredesign.History.HistoryFragment;
import kr.cnu.ai.lth.adventuredesign.Shelter.ShelterFragment;

public class MainActivity extends AppCompatActivity {
    Manager manager = Manager.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FragmentManager fm;
    ShelterFragment shelterFragment;
    HistoryFragment historyFragment;

    Button startButton;
    DrawerLayout drawerLayout;
    NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        checkPermissions();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        try {
            manager.LoadDB(this);
        } catch (Exception e) {
            Log.e(manager.TAG, e.getMessage());
        }

        SharedPreferences perf = getSharedPreferences("setting", MODE_PRIVATE);
        manager.getSettings().setAlarmVolume(perf.getInt("vol", 100));
        manager.getSettings().setVentType(VentType.valueOf(perf.getString("ventType", "NO_SOUND")));
        manager.getSettings().setNaviType(NaviType.valueOf(perf.getString("naviType", "NONE")));
        manager.getSettings().setShelterLimit(perf.getInt("shelterLimit", 10));

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        drawerLayout = findViewById(R.id.rootLayout);
        navView = findViewById(R.id.nav_view);

        toolbar.setNavigationOnClickListener(v -> {
            drawerLayout.openDrawer(navView);
        });

        navView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    drawerLayout.closeDrawer(navView);
                    break;
                case R.id.logout:
                    if (manager.isDriveServiceRunning(this, DuringDrive.class)) {
                        Toast.makeText(this, "?????? ????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.setting:
                    intent = new Intent(this, SettingActivity.class);
                    startActivity(intent);
                    break;
            }

            return false;
        });

        fm = getSupportFragmentManager();
        shelterFragment = new ShelterFragment();
        historyFragment = new HistoryFragment();
        ChangeView(0);

        LinearLayout shelterButton = findViewById(R.id.shelterButton);
        LinearLayout historyButton = findViewById(R.id.historyButton);
        startButton = findViewById(R.id.startButton);

        shelterButton.setOnClickListener(v -> viewShelterList());
        historyButton.setOnClickListener(v -> viewHistoryList());
        startButton.setOnClickListener(v -> startDrive());

        updateButton();

        db.collection("user").document(mAuth.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        new Handler().post(() -> {
                            if (task.getResult().get("name") != null) {
                                ((TextView) navView.getHeaderView(0).findViewById(R.id.headerID)).setText(task.getResult().get("name").toString());
                            } else {
                                Toast.makeText(this, "????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                                Intent intent = new Intent(this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navView)) {
            drawerLayout.closeDrawer(navView);
        } else {
            super.onBackPressed();
        }
    }

    private void updateButton() {
        if (manager.isDriveServiceRunning(this, DuringDrive.class)) {
            startButton.setText("?????? ??????");
        } else {
            startButton.setText("?????? ??????");
        }
    }

    public void viewShelterList() {
        ChangeView(0);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        shelterFragment.RefreshShelters(manager.getSettings().getShelterLimit(), locationManager);
    }

    public void viewHistoryList() {
        ChangeView(1);
    }

    public void startDrive() {
        //manager.insertHistory(rnd.nextInt(10) + 1, rnd.nextInt(265) + 20);
        Intent intent = new Intent(this, DuringDrive.class);
        if (startButton.getText().toString().equals("?????? ??????")) {
            intent.setAction("STOP");
        }
        startForegroundService(intent);
        new Handler().postDelayed(this::updateButton, 250);
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

    private void checkPermissions() {
        TedPermission.create()
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {

                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(getApplicationContext(), "GPS ????????? ?????????????????????. ????????? ???????????? ?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setDeniedMessage("GPS ?????? ????????? ???????????? ?????? ????????? ????????? ??? ????????????.")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();

        TedPermission.create()
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {

                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(getApplicationContext(), "????????? ????????? ?????????????????????. ????????? ???????????? ?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setDeniedMessage("????????? ?????? ????????? ?????? ?????? ????????? ????????? ??? ????????????.")
                .setPermissions(Manifest.permission.CAMERA)
                .check();
    }
}