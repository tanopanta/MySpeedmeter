package com.example.tattata.myspeedometer;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener{
    private LocationManager locationManager;
    private double locationX;
    private double locationY;
    private double tmpX;
    private double tmpY;

    private TextView resultTextView;
    Handler handler;
    Runnable r;

    private static final long MIN_MILLI_SECONDS = 2000;
    private static final float MIN_METRE = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        resultTextView = (TextView)findViewById(R.id.resultTextView);

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            // GPSを設定するように促す
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_MILLI_SECONDS, MIN_METRE, this);
        } catch (SecurityException se) {
            showMessage("権限がないよ");
            finish();
        }


        tmpX = 1024f;
        handler = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                float[] result = new float[3];
                if(tmpX != 1024f) {
                    //tmpXがすでに変更されいるとき
                    Location.distanceBetween(tmpX, tmpY, locationX, locationY, result);
                    float distance = result[0];
                    float speed = distance / 3f * 3.6f;
                    resultTextView.setText(String.format("%.2f km/h", speed));
                }

                tmpX = locationX;
                tmpY = locationY;

                handler.removeCallbacks(this);
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(r);
    }

    @Override
    public void onLocationChanged(Location location) {
        locationX = location.getLatitude();
        locationY = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("debug", "LocationProvider.AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        showMessage("機能を利用するためにはGPSを有効にしてください");
    }
    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
