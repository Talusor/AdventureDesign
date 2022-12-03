package kr.cnu.ai.lth.adventuredesign.History;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
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

    Button lastMonth, thisMonth, nextMonth;
    List<Button> buttons = new ArrayList<>();

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        handler = new Handler();
        loading = view.findViewById(R.id.LoadingBar);
        loading.setVisibility(View.INVISIBLE);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        Calendar cal = Calendar.getInstance();

        lastMonth = view.findViewById(R.id.lastMonth);
        thisMonth = view.findViewById(R.id.thisMonth);
        nextMonth = view.findViewById(R.id.nextMonth);

        thisMonth.setText(String.format("%d / %d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
        cal.add(Calendar.MONTH, -1);
        lastMonth.setText(String.format("%d / %d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
        cal.add(Calendar.MONTH, 2);
        nextMonth.setText(String.format("%d / %d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));

        lastMonth.setOnClickListener(this::onClickButton);
        thisMonth.setOnClickListener(this::onClickButton);
        nextMonth.setOnClickListener(this::onClickButton);

        buttons.add(lastMonth);
        buttons.add(thisMonth);
        buttons.add(nextMonth);

        thisMonth.performClick();

        return view;
    }

    private void onClickButton(View v) {
        for (Button btn : buttons) {
            if (v == btn) {
                btn.setEnabled(false);
                btn.setBackgroundTintList(
                        ContextCompat.getColorStateList(
                                getActivity(),
                                com.google.android.material.R.color.design_default_color_primary_variant
                        )
                );
            } else {
                btn.setEnabled(true);
                btn.setBackgroundTintList(
                        ContextCompat.getColorStateList(
                                getActivity(),
                                com.google.android.material.R.color.design_default_color_primary
                        )
                );
            }
        }

        String[] temp = ((Button)v).getText().toString().split(" / ");
        int year = Integer.parseInt(temp[0]);
        int month = Integer.parseInt(temp[1]);
        new Thread(() -> {
            handler.post(() -> {
                recyclerView.setVisibility(View.INVISIBLE);
                loading.setVisibility(View.VISIBLE);
            });

            adapter.ClearData();
            adapter.AddData(manager.getHistories(year, month));

            handler.post(() -> {
                recyclerView.setVisibility(View.VISIBLE);
                loading.setVisibility(View.INVISIBLE);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
