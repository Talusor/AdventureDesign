package kr.cnu.ai.lth.adventuredesign;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ShelterAdapter extends RecyclerView.Adapter<ShelterAdapter.ShelterViewHolder> implements OnShelterItemClickListener{
    List<Shelter> data = new ArrayList<>();
    OnShelterItemClickListener listener;

    @NonNull
    @Override
    public ShelterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new ShelterViewHolder(inflater.inflate(R.layout.shelter_info, parent, false), this);
    }

    public void setOnShelterItemClickListener(OnShelterItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onItemClick(ShelterViewHolder holder, View view, int position) {
        if (listener != null)
            listener.onItemClick(holder, view, position);
    }

    public void clearData() {
        data.clear();
    }

    public void addData(List<Shelter> data) {
        this.data.addAll(data);
    }

    public Shelter getData(int index) {
        return data.get(index);
    }

    @Override
    public void onBindViewHolder(@NonNull ShelterViewHolder holder, int position) {
        holder.setData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ShelterViewHolder extends RecyclerView.ViewHolder {
        private final TextView NameText;
        private final TextView DistanceText;
        private final TextView ParkingText;
        private final TextView ToiletText;

        public ShelterViewHolder(@NonNull View view, final OnShelterItemClickListener listener) {
            super(view);
            NameText = view.findViewById(R.id.nameText);
            DistanceText = view.findViewById(R.id.distanceText);
            ParkingText = view.findViewById(R.id.parkingText);
            ToiletText = view.findViewById(R.id.toiletText);

            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null)
                    listener.onItemClick(this, view, position);
            });
        }

        public void setData(@NonNull Shelter shelter) {
            NameText.setText(String.format("%s 졸음 쉼터 (%s)", shelter.getName(), shelter.getEndRoad()));
            DistanceText.setText(String.format("%.2f KM", shelter.getDistanceFromLatLonInKm()));
            ParkingText.setText(String.format("주차 면수 : %d",shelter.getParkingSpace()));
            if (shelter.getHasToilet())
                ToiletText.setText("화장실 여부 : Y");
            else
                ToiletText.setText("화장실 여부 : N");
        }
    }
}
