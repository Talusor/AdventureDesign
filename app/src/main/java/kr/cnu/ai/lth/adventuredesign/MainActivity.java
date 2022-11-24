package kr.cnu.ai.lth.adventuredesign;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    final String TAG = "[ADV]";
    ShelterAdapter adapter = new ShelterAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        LinearLayout shelterButton = findViewById(R.id.shelterButton);
        shelterButton.setOnClickListener(v -> btnClick());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.setOnShelterItemClickListener((holder, view, position) -> {
            try {
                Shelter data = adapter.getData(position);
                Log.d(TAG, String.format("Clicked (%d), %s", position, data.getName()));
                String url =
                        "nmap://navigation?dlat=" + data.getLat() +
                                "&dlng=" + data.getLng() +
                                "&dname=" + URLEncoder.encode(data.getName() + " 졸음 쉼터", "UTF-8") +
                                "&appname=testApp";

                Log.d(TAG, url);

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                getApplicationContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception ignored) {  }
        });
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
        File file = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/shelter.db");

        if (!file.exists()) {
            Log.d(TAG, "Make file");
            try (
                    InputStream is = getApplicationContext().getResources().openRawResource(R.raw.shelter);
                    OutputStream output = new FileOutputStream(getApplicationContext().getFilesDir().getAbsolutePath() + "/shelter.db")
            ) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            } catch (Exception ignored) {
            }
        } else {
            Log.d(TAG, "File exists");

            DBHelper dbHelper = new DBHelper(this);
            List<Shelter> temp = dbHelper.getShelters();
            Log.d(TAG, String.format("Get %d shelters", temp.size()));
            checkPermission(() -> {
                LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc != null) {
                    double lat = loc.getLatitude();
                    double lng = loc.getLongitude();
                    Log.d(TAG, String.format("Get loc (%.3f, %.3f)", lat, lng));

                    temp.sort((o1, o2) -> {
                        double d1 = o1.getDistance(lat, lng);
                        double d2 = o2.getDistance(lat, lng);
                        if (d1 == d2)
                            return 0;
                        if (d1 - d2 > 0)
                            return 1;
                        else
                            return -1;
                    });

                    List<Shelter> sortedData = temp.stream().limit(15).collect(Collectors.toList());
                    for (Shelter item : sortedData) {
                        item.setDistanceFromLatLonInKm(lat, lng);
                        Log.d(TAG, String.format("Nearby Shelter %s, (%.2f, %.2f)",
                                item.getName(),
                                item.getLat(),
                                item.getLng()));
                    }
                    adapter.clearData();
                    adapter.addData(sortedData);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}