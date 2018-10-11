package com.example.kuba.weatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuba.weatherapp.model.CityModel;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView selectCity, cityField, detailsField, currentTemperatureField, humidity_field, pressure_field, weatherIcon, updatedField;
    ProgressBar loader;
    FloatingActionButton fabList;
    Typeface weatherFont;
    String city = "Warsaw, PL";
    String OPEN_WEATHER_MAP_API = "4d16fb313e7d9e9769f7fecc354af62e";

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        initViews();
        downloadCityData(city);
    }

    @Override
    protected void onPause() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        super.onPause();
    }

    //Initialize views
    private void initViews(){
        loader = (ProgressBar) findViewById(R.id.loader);
        fabList = (FloatingActionButton) findViewById(R.id.fab_list);
        selectCity = (TextView) findViewById(R.id.selectCity);
        cityField = (TextView) findViewById(R.id.city_field);
        updatedField = (TextView) findViewById(R.id.updated_field);
        detailsField = (TextView) findViewById(R.id.details_field);
        currentTemperatureField = (TextView) findViewById(R.id.current_temperature_field);
        humidity_field = (TextView) findViewById(R.id.humidity_field);
        pressure_field = (TextView) findViewById(R.id.pressure_field);
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);

        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectCityDialog();
            }
        });

        fabList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCityList();
            }
        });
    }

    //Create and show dialog with form to change city
    private void showSelectCityDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.change_city);
        final EditText input = new EditText(MainActivity.this);
        input.setText(city);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialogBuilder.setView(input);

        alertDialogBuilder.setPositiveButton(R.string.change,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        city = input.getText().toString();
                        downloadCityData(city);
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog = alertDialogBuilder.show();
    }

    //Open new screen with city list
    private void openCityList(){
        Intent intent = new Intent(MainActivity.this, CityListActivity.class);
        startActivity(intent);
    }

    //Check if download data is possible
    public void downloadCityData(String query) {
        if (Function.isNetworkAvailable(getApplicationContext())) {
            DownloadWeather task = new DownloadWeather();
            task.execute(query);
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_network, Toast.LENGTH_LONG).show();
        }
    }

    //Asynchronously download weather data for city
    class DownloadWeather extends AsyncTask < String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoader();
        }
        protected String doInBackground(String...args) {
            String xml = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] +
                    "&units=metric&appid=" + OPEN_WEATHER_MAP_API);
            return xml;
        }
        @Override
        protected void onPostExecute(String json) {
            CityModel city = Function.getCityModelFromJson(json);
            if (city != null){
                updateCityUI(city);
            }else{
                Toast.makeText(getApplicationContext(), R.string.error_select_city, Toast.LENGTH_SHORT).show();
            }
            hideLoader();
        }

    }

    //Update city data
    private void updateCityUI(CityModel cityModel){
        cityField.setText(cityModel.getName());
        detailsField.setText(cityModel.getDescription());
        currentTemperatureField.setText(String.format("%.2f", cityModel.getTemp()) + "°");
        humidity_field.setText("Wilgotność: " + cityModel.getHumidity() + "%");
        pressure_field.setText("Ciśnienie: " + cityModel.getPressure() + " hPa");
        updatedField.setText(cityModel.getUpdated());
        weatherIcon.setText(Html.fromHtml(cityModel.getIcon()));
    }

    private void showLoader(){
        loader.setVisibility(View.VISIBLE);
    }

    private void hideLoader(){
        loader.setVisibility(View.GONE);
    }
}