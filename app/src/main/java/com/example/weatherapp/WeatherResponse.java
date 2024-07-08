package com.example.weatherapp;

import java.util.List;

// WeatherResponse.java
public class WeatherResponse {
    public WeatherMain main;
    public List<Weather> weather;

    public WeatherMain getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }
}
