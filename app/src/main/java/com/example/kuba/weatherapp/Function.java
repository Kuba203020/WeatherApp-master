package com.example.kuba.weatherapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.example.kuba.weatherapp.model.CityModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Function {


    public static boolean isNetworkAvailable(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    public static String excuteGet(String targetURL) {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Content-type", "application/json;  charset=utf-8");
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(false);

            InputStream is;
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK)
                is = connection.getErrorStream();
            else
                is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
              icon = "&#xf00d;";
            } else {
                icon = "&#xf02e;";
            }
        } else {
            switch(id) {
                case 2 : icon = "&#xf01e;";
                    break;
                case 3 : icon = "&#xf01c;";
                    break;
                case 7 : icon = "&#xf014;";
                    break;
                case 8 : icon = "&#xf013;";
                    break;
                case 6 : icon = "&#xf01b;";
                    break;
                case 5 : icon = "&#xf019;";
                    break;
            }
        }
        return icon;
    }

    //Convert json to model
    public static CityModel getCityModelFromJson(String jsonString){
        try {
            JSONObject json = new JSONObject(jsonString);

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            DateFormat df = DateFormat.getDateTimeInstance();

            long id = json.getLong("id");
            String name = json.getString("name").toUpperCase(Locale.getDefault()) +
                    ", " + json.getJSONObject("sys").getString("country");
            String description = details.getString("description").toUpperCase(Locale.getDefault());
            Double temp = main.getDouble("temp");
            String humidity = main.getString("humidity");
            String pressure = main.getString("pressure");
            long timestamp = (json.getLong("dt") * 1000);
            String update = df.format(new Date(json.getLong("dt") * 1000));
            String icon = Function.setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

            CityModel city = new CityModel(id);
            city.setName(name);
            city.setDescription(description);
            city.setTemp(temp);
            city.setHumidity(humidity);
            city.setPressure(pressure);
            city.setTimestamp(timestamp);
            city.setUpdated(update);
            city.setIcon(icon);

            return city;

        } catch (JSONException e) {
            return null;
        }
    }

    //Convert json to array with model
    public static List<CityModel> getCityModelsFromJson(String jsonString){

        List<CityModel> cities = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray list = json.getJSONArray("list");

            for (int x = 0; x < list.length(); x++){
                JSONObject jsonItem = list.getJSONObject(x);

                JSONObject details = jsonItem.getJSONArray("weather").getJSONObject(0);
                JSONObject main = jsonItem.getJSONObject("main");
                DateFormat df = DateFormat.getDateTimeInstance();

                long id = jsonItem.getLong("id");
                String name = jsonItem.getString("name").toUpperCase(Locale.getDefault()) +
                        ", " + jsonItem.getJSONObject("sys").getString("country");
                String description = details.getString("description").toUpperCase(Locale.getDefault());
                Double temp = main.getDouble("temp");
                String humidity = main.getString("humidity");
                String pressure = main.getString("pressure");
                long timestamp = (jsonItem.getLong("dt") * 1000);
                String update = df.format(new Date(jsonItem.getLong("dt") * 1000));
                String icon = Function.setWeatherIcon(details.getInt("id"),
                        jsonItem.getJSONObject("sys").getLong("sunrise") * 1000,
                        jsonItem.getJSONObject("sys").getLong("sunset") * 1000);

                CityModel city = new CityModel(id);
                city.setName(name);
                city.setDescription(description);
                city.setTemp(temp);
                city.setHumidity(humidity);
                city.setPressure(pressure);
                city.setTimestamp(timestamp);
                city.setUpdated(update);
                city.setIcon(icon);

                cities.add(city);
            }

            return cities;
        } catch (JSONException e) {
            return null;
        }
    }

    //Check if timestamp hour == current hour
    public static boolean isUpToDate(long timestamp){
        Calendar calNow = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        if (cal.get(Calendar.YEAR) != calNow.get(Calendar.YEAR) ||
                cal.get(Calendar.MONTH) != calNow.get(Calendar.MONTH) ||
                cal.get(Calendar.DAY_OF_MONTH) != calNow.get(Calendar.DAY_OF_MONTH) ||
                cal.get(Calendar.HOUR_OF_DAY) != calNow.get(Calendar.HOUR_OF_DAY)){

            return false;
        }

        return true;
    }

}