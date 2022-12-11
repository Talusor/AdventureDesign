package kr.cnu.ai.lth.adventuredesign.Shelter;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import kr.cnu.ai.lth.adventuredesign.Manager;
import kr.cnu.ai.lth.adventuredesign.R;
import kr.cnu.ai.lth.adventuredesign.Shelter.Shelter;
import kr.cnu.ai.lth.adventuredesign.Shelter.ShelterAdapter;

public class ShelterFragment extends Fragment {
    Manager manager = Manager.getInstance();
    ShelterAdapter adapter = new ShelterAdapter();
    Handler handler;
    ProgressBar loading;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shelter, container, false);

        handler = new Handler();

        loading = view.findViewById(R.id.LoadingBar);
        loading.setVisibility(View.INVISIBLE);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        adapter.setOnShelterItemClickListener((holder, unusedView, position) -> {
            try {
                Shelter data = adapter.getData(position);
                Log.d(manager.TAG, String.format("Clicked (%d), %s", position, data.getName()));
                String url = manager.getSettings().getUrlScheme(data.getLat(), data.getLng(), data.getName() + " 졸음 쉼터");

                Log.d(manager.TAG, url);

                if (url != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    getContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            } catch (Exception ignored) {
            }
        });

        return view;
    }

    public void ClearData() {
        adapter.clearData();
        adapter.notifyDataSetChanged();
    }

    public void RefreshShelters(int limit, LocationManager locManager) {
        checkPermission(() -> {
            handler.post(() -> {
                recyclerView.setVisibility(View.INVISIBLE);
                loading.setVisibility(View.VISIBLE);
            });

            Location last = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) {
                if (TimeUnit.NANOSECONDS.toMillis(SystemClock.elapsedRealtimeNanos()
                        - last.getElapsedRealtimeNanos()) >= 5000) {
                    locManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, loc -> {
                        double lat = loc.getLatitude();
                        double lng = loc.getLongitude();

                        List<Shelter> data = manager.getClosestShelters(lat, lng, limit);

                        adapter.clearData();
                        adapter.addData(data);

                        handler.post(() -> {
                            recyclerView.setVisibility(View.VISIBLE);
                            loading.setVisibility(View.INVISIBLE);
                            adapter.notifyDataSetChanged();
                        });
                    }, null);
                } else {
                    double lat = last.getLatitude();
                    double lng = last.getLongitude();

                    List<Shelter> data = manager.getClosestShelters(lat, lng, limit);

                    adapter.clearData();
                    adapter.addData(data);

                    handler.post(() -> {
                        recyclerView.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.INVISIBLE);
                        adapter.notifyDataSetChanged();
                    });
                }
            } else {
                locManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, loc -> {
                    double lat = loc.getLatitude();
                    double lng = loc.getLongitude();

                    List<Shelter> data = manager.getClosestShelters(lat, lng, limit);

                    adapter.clearData();
                    adapter.addData(data);

                    handler.post(() -> {
                        recyclerView.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.INVISIBLE);
                        adapter.notifyDataSetChanged();
                    });
                }, null);

                locManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, loc -> {

                }, null);
            }
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
                        Toast.makeText(getActivity(), "GPS 권한이 거부되었습니다. 기능을 사용하기 위해 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setDeniedMessage("GPS 권한 거부시 졸음쉼터 검색 기능을 사용할 수 없습니다.")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }
}