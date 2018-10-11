package com.example.kuba.weatherapp.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kuba.weatherapp.R;
import com.example.kuba.weatherapp.model.CityModel;

import java.util.ArrayList;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter {

    private List<CityModel> cities = new ArrayList<>();
    private OnItemClickListener listener;

    public CityAdapter() {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((CityViewHolder) holder).bindData(cities.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.item_city;
    }

    public List<CityModel> getCities() {
        return cities;
    }

    public void setCities(List<CityModel> cities) {
        this.cities = cities;
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(CityModel item);
    }
}
