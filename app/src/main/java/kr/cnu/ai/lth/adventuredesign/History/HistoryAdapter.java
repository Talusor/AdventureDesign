package kr.cnu.ai.lth.adventuredesign.History;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.cnu.ai.lth.adventuredesign.R;
import kr.cnu.ai.lth.adventuredesign.Shelter.ShelterAdapter;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    List<History> data = new ArrayList<>();

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new HistoryViewHolder(inflater.inflate(R.layout.history_info, parent, false));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void AddData(History item) {
        data.add(item);
    }

    public void AddData(List<History> items) {
        data.addAll(items);
    }

    public void ClearData() {
        data.clear();
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.setData(data.get(position));
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView DateText;
        private final TextView DetectText;
        private final TextView DurationText;
        private final ImageView HistoryIcon;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            DateText = itemView.findViewById(R.id.dateText);
            DetectText = itemView.findViewById(R.id.detectText);
            DurationText = itemView.findViewById(R.id.durationText);
            HistoryIcon = itemView.findViewById(R.id.historyIcon);
        }

        public void setData(History data) {
            DateText.setText(String.format(
                    "%02d월 %02d일",
                    data.getDate().getMonth(),
                    data.getDate().getDay()
            ));

            DetectText.setText(String.format(
                    "졸음 %d회 감지",
                    data.getCntOfDetect()
            ));

            DurationText.setText(String.format(
                    "%d min",
                    data.getDuration()
            ));

            if (data.getCntOfDetect() >= 3) {
                HistoryIcon.setImageTintList(ColorStateList.valueOf(Color.RED));
                DetectText.setTextColor(Color.RED);
            } else {
                HistoryIcon.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                DetectText.setTextColor(Color.GREEN);
            }
        }
    }
}
