package com.example.kuba.weatherapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuba.weatherapp.adapter.CityAdapter;
import com.example.kuba.weatherapp.model.CityModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CityListActivity extends AppCompatActivity {

    final static String OPEN_WEATHER_MAP_API = "4d16fb313e7d9e9769f7fecc354af62e";

    ProgressBar loaderView;
    RecyclerView recyclerView;
    FloatingActionButton fabView;
    Typeface weatherFont;
    LinearLayout noDataView;

    private String cityHint = "Warszawa";
    private CityAdapter adapter = new CityAdapter();
    private List<CityModel> cities = new ArrayList<>();
    private RealmResults<CityModel> cityResults;
    private Realm realm;

    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_city_list);

        showLoader();

        initViews();
        initList();
        initRealm();
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
        fabView = (FloatingActionButton) findViewById(R.id.fab_add);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_list);
        noDataView = (LinearLayout) findViewById(R.id.no_data);
        loaderView = (ProgressBar) findViewById(R.id.loader);
        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf");

        fabView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCityDialog();
            }
        });
    }

    //Initialize list
    private void initList(){
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setListener(new CityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CityModel item) {
                showCityDialog(item);
            }
        });
    }

    //Initialize realm
    private void initRealm(){
        realm = Realm.getDefaultInstance();
        cityResults = realm.where(CityModel.class).findAllAsync();
        if (cityResults.size() > 0){
            hideNoData();
            cities = realm.copyFromRealm(cityResults);
            adapter.setCities(cities);

            if (!Function.isUpToDate(cities.get(0).getTimestamp())){
                downloadUpdateCitiesData(cities);
            }
        }else{
            showNoData();
        }
        hideLoader();

        cityResults.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<CityModel>>() {
            @Override
            public void onChange(RealmResults<CityModel> results, OrderedCollectionChangeSet changeSet) {
                Log.e("test", "onChange");
                cities = realm.copyFromRealm(results);
                adapter.setCities(cities);
                adapter.notifyDataSetChanged();
                hideLoader();

                if (results.size() > 0){
                    hideNoData();
                }else{
                    showNoData();
                }
            }
        });
    }

    //Add city to realm
    private void addCity(CityModel city) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(city);
        realm.commitTransaction();
    }

    //Update cities in realm
    private void updateCities(List<CityModel> cities) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(cities);
        realm.commitTransaction();
    }

    //Remove city from realm
    private void removeCity(CityModel city) {
        realm.beginTransaction();
        CityModel cityResult = realm.where(CityModel.class).equalTo("id", city.getId()).findFirst();
        if (cityResult != null){
            Log.e("test", "deleteFromRealm");
            cityResult.deleteFromRealm();
        }
        realm.commitTransaction();
    }

    //Check if download data is possible
    private void dowloadCityData(String city){
        if (Function.isNetworkAvailable(getApplicationContext())) {
            CityListActivity.DownloadWeather task = new CityListActivity.DownloadWeather();
            task.execute(city);
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_network, Toast.LENGTH_LONG).show();
        }
    }

    //Asynchronously download weather data for city
    class DownloadWeather extends AsyncTask< String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoader();
        }
        protected String doInBackground(String...args) {
            String json = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] +
                    "&units=metric&appid=" + OPEN_WEATHER_MAP_API);
            return json;
        }
        @Override
        protected void onPostExecute(String json) {
            CityModel city = Function.getCityModelFromJson(json);
            if (city != null){
                addCity(city);
            }else{
                Toast.makeText(getApplicationContext(), R.string.error_wrong_city, Toast.LENGTH_SHORT).show();
                hideLoader();
            }
        }
    }

    //Create string with ids for update
    private String createIdList(List<CityModel> cities){
        String ids = "";
        for (CityModel cityModel : cities){
            if (!ids.isEmpty()){
                ids += ",";
            }
            ids += String.valueOf(cityModel.getId());
        }
        return ids;
    }

    //Check if download data is possible
    private void downloadUpdateCitiesData(List<CityModel> cities){
        if (Function.isNetworkAvailable(getApplicationContext())) {
            CityListActivity.DownloadWeathers task = new CityListActivity.DownloadWeathers();
            task.execute(createIdList(cities));
        } else {
            hideLoader();
            Toast.makeText(getApplicationContext(), R.string.error_network, Toast.LENGTH_LONG).show();
        }
    }

    //Asynchronously download weathers data for cities
    class DownloadWeathers extends AsyncTask< String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoader();
        }
        protected String doInBackground(String...args) {
            String json = Function.excuteGet("http://api.openweathermap.org/data/2.5/group?id=" + args[0] +
                    "&units=metric&appid=" + OPEN_WEATHER_MAP_API);
            return json;
        }
        @Override
        protected void onPostExecute(String json) {
            List<CityModel> newCities = Function.getCityModelsFromJson(json);
            if (newCities != null && newCities.size() > 0){
                updateCities(newCities);
            }
        }
    }

    //Create and show dialog with form to add city
    private void showAddCityDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CityListActivity.this);
        alertDialogBuilder.setTitle(R.string.add_city);
        final EditText input = new EditText(CityListActivity.this);
        input.setHint(cityHint);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialogBuilder.setView(input);

        alertDialogBuilder.setPositiveButton(R.string.add,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dowloadCityData(input.getText().toString());
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

    //Create and show dialog with city details
    private void showCityDialog(final CityModel cityModel){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CityListActivity.this);
        alertDialogBuilder.setTitle(cityModel.getName());

        //Create fields
        final TextView icon = createTexView();
        icon.setTypeface(weatherFont);
        icon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
        icon.setText(Html.fromHtml(cityModel.getIcon()));

        final TextView description = createTexView();
        description.setText(cityModel.getDescription());

        final TextView temp = createTexView();
        temp.setText(String.format("%.2f", cityModel.getTemp()) + "°");

        final TextView humidity = createTexView();
        humidity.setText("Wilgotność: " + cityModel.getHumidity() + "%");

        final TextView pressure = createTexView();
        pressure.setText("Ciśnienie: " + cityModel.getPressure() + " hPa");

        //Create dialog layout
        final LinearLayout layout = new LinearLayout(CityListActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(2, 2, 2, 2);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        layout.addView(icon);
        layout.addView(description);
        layout.addView(temp);
        layout.addView(humidity);
        layout.addView(pressure);

        //Add fields to dialog
        alertDialogBuilder.setView(layout);

        alertDialogBuilder.setPositiveButton(R.string.delete,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showLoader();
                        removeCity(cityModel);
                        dialog.cancel();
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

    private void showLoader(){
//        progressDialog = new ProgressDialog(CityListActivity.this);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        Log.e("test", "showLoader");
        progressDialog = ProgressDialog.show(this,null,null);

        //Create dialog layout
        final LinearLayout layout = new LinearLayout(CityListActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(10, 10, 10, 10);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        layout.addView(new ProgressBar(this));

        progressDialog.setContentView(layout);
//        progressDialog.setCancelable(false);
    }

    private void hideLoader(){
        Log.e("test", "hideLoader");
        progressDialog.dismiss();
        progressDialog.cancel();
    }

    private void showNoData(){
        noDataView.setVisibility(View.VISIBLE);
    }

    private void hideNoData(){
        noDataView.setVisibility(View.GONE);
    }

    //Create TextView field
    private TextView createTexView(){
        TextView textView = new TextView(CityListActivity.this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return textView;
    }
}
