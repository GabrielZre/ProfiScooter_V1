package com.example.profiscooter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    Context context;

    ArrayList<Trip> list;

    public HistoryAdapter(Context context, ArrayList<Trip> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.historyitem,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Trip trip = list.get(position);
        holder.tripName.setText(trip.getTripName());
        holder.distance.setText(trip.getTotalDistance());
        holder.distanceTime.setText(trip.getDistanceTime());
        holder.averageSpeed.setText(trip.getAverageSpeed());
        holder.batteryDrain.setText(trip.getBatteryDrain());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends  RecyclerView.ViewHolder{

        TextView tripName, distance, distanceTime, averageSpeed, batteryDrain;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tripName = itemView.findViewById(R.id.tvTripName);
            distance = itemView.findViewById(R.id.tvHistoryDistance);
            distanceTime = itemView.findViewById(R.id.tvHistoryDistanceTime);
            averageSpeed = itemView.findViewById(R.id.tvHistoryAvgSpeed);
            batteryDrain = itemView.findViewById(R.id.tvHistoryBatteryDrain);

        }
    }
}
