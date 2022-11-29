package kr.cnu.ai.lth.adventuredesign.History;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

import kr.cnu.ai.lth.adventuredesign.Manager;
import kr.cnu.ai.lth.adventuredesign.R;
import kr.cnu.ai.lth.adventuredesign.Shelter.Shelter;

public class HistoryFragment extends Fragment {
    Manager manager = Manager.getInstance();
    HistoryAdapter adapter = new HistoryAdapter();
    Handler handler;
    ProgressBar loading;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        handler = new Handler();
        loading = view.findViewById(R.id.LoadingBar);
        loading.setVisibility(View.INVISIBLE);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void Test() {
        new Thread(() -> {
            handler.post(() -> {
                recyclerView.setVisibility(View.INVISIBLE);
                loading.setVisibility(View.VISIBLE);
            });

            try {
                Thread.sleep(2500);
            } catch (Exception ignored) {
            }

            adapter.ClearData();
            adapter.AddData(new History(
                    new Date(),
                    0,
                    10
            ));
            adapter.AddData(new History(
                    new Date(),
                    1,
                    42
            ));
            adapter.AddData(new History(
                    new Date(),
                    3,
                    240
            ));

            handler.post(() -> {
                recyclerView.setVisibility(View.VISIBLE);
                loading.setVisibility(View.INVISIBLE);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
