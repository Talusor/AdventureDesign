package kr.cnu.ai.lth.adventuredesign;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

import kr.cnu.ai.lth.adventuredesign.Shelter.Shelter;
import kr.cnu.ai.lth.adventuredesign.Shelter.ShelterAdapter;

public class MainActivity extends AppCompatActivity {
    Manager manager = Manager.getInstance();

    FragmentManager fm;
    ShelterFragment shelterFragment;

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

        fm = getSupportFragmentManager();
        shelterFragment = new ShelterFragment();
        ChangeView(0);

        LinearLayout shelterButton = findViewById(R.id.shelterButton);
        shelterButton.setOnClickListener(v -> btnClick());
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
            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) {
                double lat = loc.getLatitude();
                double lng = loc.getLongitude();
                shelterFragment.RefreshShelters(lat, lng, 10);
            }
        });
    }

    private void ChangeView(int ID) {
        FragmentTransaction ft = fm.beginTransaction();

        switch (ID) {
            case 1:
                ft.replace(R.id.frameView, shelterFragment).commitAllowingStateLoss();
                break;
            default:
                ft.replace(R.id.frameView, shelterFragment).commitAllowingStateLoss();
                break;
        }
    }
}