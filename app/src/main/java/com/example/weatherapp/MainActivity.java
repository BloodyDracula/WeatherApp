package com.example.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Geocoder;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView weatherTextView;
    private EditText cityEditText;
    private Spinner unitSpinner;
    private ImageView weatherIconImageView;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherTextView = findViewById(R.id.weatherTextView);
        cityEditText = findViewById(R.id.cityEditText);
        unitSpinner = findViewById(R.id.unitSpinner);
        weatherIconImageView = findViewById(R.id.weatherIconImageView);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.units_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);

        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateWeather();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Ничего не делаем
            }
        });

        Button getWeatherButton = findViewById(R.id.getWeatherButton);
        getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWeather();
            }
        });
    }

    private void updateWeather() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            String cityName = cityEditText.getText().toString().trim();
            if (!cityName.isEmpty()) {
                getWeatherForCity(cityName);
            } else {
                getLocation();
            }
        }
    }

    private void getWeatherForCity(String cityName) {
        String apiKey = "c6652e26c654de5fc15ceb0af9ced0b1";
        String units = unitSpinner.getSelectedItem().toString().equals("Celsius") ? "metric" : "imperial";

        WeatherApi weatherApi = RetrofitClient.create();
        Call<WeatherResponse> call = weatherApi.getCurrentWeatherByCity(cityName, units, apiKey);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    double temperature = weatherResponse.getMain().getTemp();
                    String description = weatherResponse.getWeather().get(0).getDescription();
                    int weatherIconResource = getWeatherIconResource(description);

                    String weatherString;
                    if (units.equals("metric")) {
                        weatherString = "Temperature: " + temperature + "°C\nDescription: " + description;
                    } else {
                        double tempInFahrenheit = (temperature * 9 / 5) + 32;
                        weatherString = "Temperature: " + tempInFahrenheit + "°F\nDescription: " + description;
                    }
                    weatherTextView.setText(weatherString);
                    weatherIconImageView.setImageResource(weatherIconResource);
                } else {
                    weatherTextView.setText("Failed to get weather data");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                weatherTextView.setText("Failed to get weather data: " + t.getMessage());
            }
        });
    }


    private void getLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Обработка отсутствия разрешений на доступ к местоположению
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            getWeatherForLocation(latitude, longitude);

                            // Определение названия города
                            Geocoder geocoder = new Geocoder(MainActivity.this);
                            try {
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (!addresses.isEmpty()) {
                                    String cityName = addresses.get(0).getLocality();
                                    // Установка названия города в TextView
                                    TextView cityNameTextView = findViewById(R.id.cityName);
                                    cityNameTextView.setText(cityName);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);

                        } else {
                            Toast.makeText(MainActivity.this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    private void getWeatherForLocation(double latitude, double longitude) {
        String apiKey = "c6652e26c654de5fc15ceb0af9ced0b1";
        String units = unitSpinner.getSelectedItem().toString().equals("Celsius") ? "metric" : "imperial";

        WeatherApi weatherApi = RetrofitClient.create();
        Call<WeatherResponse> call = weatherApi.getCurrentWeatherByCoordinates(latitude, longitude, units, apiKey);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    double temperature = weatherResponse.getMain().getTemp();
                    String description = weatherResponse.getWeather().get(0).getDescription();
                    int weatherIconResource = getWeatherIconResource(description);

                    String weatherString;
                    if (units.equals("metric")) {
                        weatherString = "Temperature: " + temperature + "°C\nDescription: " + description;
                    } else {
                        double tempInFahrenheit = (temperature * 9 / 5) + 32;
                        weatherString = "Temperature: " + tempInFahrenheit + "°F\nDescription: " + description;
                    }
                    weatherTextView.setText(weatherString);
                    weatherIconImageView.setImageResource(weatherIconResource);
                } else {
                    weatherTextView.setText("Failed to get weather data");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                weatherTextView.setText("Failed to get weather data: " + t.getMessage());
            }
        });
    }

    private int getWeatherIconResource(String weatherType) {
        switch (weatherType) {
            case "clouds":
                return R.drawable.cloudy;
            case "broken clouds":
                return R.drawable.cloudy;
            case "few clouds":
                return R.drawable.cloudy;
            case "scattered clouds":
                return R.drawable.cloudy;
            case "overcast clouds":
                return R.drawable.cloudy;
            case "moderate rain":
                return R.drawable.rain;
            case "heavy rain":
                return R.drawable.rain;
            case "light rain":
                return R.drawable.rain;
            case "clear sky":
                return R.drawable.sun;
            case "thunderstorm":
                return R.drawable.storm;
            case "snow":
                return R.drawable.snow;
            default:
                return R.drawable.ic_launcher_foreground;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
