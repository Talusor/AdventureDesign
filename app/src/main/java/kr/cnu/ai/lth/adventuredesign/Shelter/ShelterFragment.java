package kr.cnu.ai.lth.adventuredesign.Shelter;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.net.URLEncoder;
import java.util.List;

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
                String url =
                        "nmap://navigation?dlat=" + data.getLat() +
                                "&dlng=" + data.getLng() +
                                "&dname=" + URLEncoder.encode(data.getName() + " 졸음 쉼터", "UTF-8") +
                                "&appname=testApp";

                Log.d(manager.TAG, url);

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                getContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception ignored) {
            }
        });

        return view;
    }

    public void ClearData() {
        adapter.clearData();
        adapter.notifyDataSetChanged();
    }

    public void RefreshShelters(double lat, double lng, int limit) {
        new Thread(() -> {
            handler.post(() -> {
                recyclerView.setVisibility(View.INVISIBLE);
                loading.setVisibility(View.VISIBLE);
            });
            List<Shelter> data = manager.getClosestShelters(lat, lng, limit);

            try {
                Thread.sleep(2500);
            } catch (Exception ignored) {}

            adapter.clearData();
            adapter.addData(data);

            handler.post(() -> {
                recyclerView.setVisibility(View.VISIBLE);
                loading.setVisibility(View.INVISIBLE);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}