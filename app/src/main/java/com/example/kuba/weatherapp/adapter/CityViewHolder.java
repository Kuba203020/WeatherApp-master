package com.example.kuba.weatherapp.adapter;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kuba.weatherapp.R;
import com.example.kuba.weatherapp.model.CityModel;

import java.util.Locale;

public class CityViewHolder extends RecyclerView.ViewHolder {

    private LinearLayout container;
    private TextView cityTextView;
    private TextView temperatureTextView;
    private TextView weatherIcon;
    private Typeface weatherFont;

    public CityViewHolder(final View itemView) {
        super(itemView);
        container = (LinearLayout) itemView.findViewById(R.id.container);
        cityTextView = (TextView) itemView.findViewById(R.id.city_field);
        temperatureTextView = (TextView) itemView.findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView) itemView.findViewById(R.id.weather_icon);
        weatherFont = Typeface.createFromAsset(itemView.getResources().getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);
    }

    //Set city data to item view
    public void bindData(final CityModel cityModel, final CityAdapter.OnItemClickListener listener) {
        cityTextView.setText(cityModel.getName());
        temperatureTextView.setText(String.format("%.2f", cityModel.getTemp()) + "°");
        weatherIcon.setText(Html.fromHtml(cityModel.getIcon()));

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onItemClick(cityModel);
                }
            }
        });
    }
}