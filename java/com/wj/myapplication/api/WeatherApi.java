package com.wj.myapplication.api;

import android.util.Log;

import com.google.gson.Gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class WeatherApi {
    @FunctionalInterface
    public interface OnWeatherReceivedCallback {
        void onWeatherReceived(Weather weather, Wind wind);
    }

    private static final String TAG = "WeatherApi";

    private static URL getUrlForCity(String city) throws MalformedURLException {
        final StringBuilder URL = new StringBuilder("https://api.openweathermap.org/data/2.5/weather?q=");
        final String APP_ID = "143454aa39bbe3442a890cdbf3f9db36";
        URL.append(city.toLowerCase());
        URL.append("&APPID=");
        URL.append(APP_ID);
        return new URL(URL.toString());
    }




    public static void getWeatherForCity(String city, WeatherApi.OnWeatherReceivedCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) getUrlForCity(city).openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();

                        JsonElement weatherJson = json.get("main");
                        Weather weather = new Gson().fromJson(weatherJson, Weather.class);

                        JsonElement windJson = json.get("wind");
                        Wind wind = new Gson().fromJson(windJson, Wind.class);

                        callback.onWeatherReceived(weather, wind);
                    } else {
                        Log.i(TAG, "instance initializer: err");
                    }

                } catch (IOException e) {
                    // Handle the exception appropriately (e.g., log the error, return an error response, etc.)
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
