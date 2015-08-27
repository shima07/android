package com.example.shima07.simple_gps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.util.Log;

import android.app.Service;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.location.GpsSatellite;
import android.location.LocationListener;
import android.location.Location;
import android.location.LocationProvider;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements LocationListener,GpsStatus.Listener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_UPDATE_MIN_TIME = 0;
    private static final int LOCATION_UPDATE_MIN_DISTANCE = 0;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "onCreate");

        mLocationManager = (LocationManager)this.getSystemService(Service.LOCATION_SERVICE);
        mLocationManager.addGpsStatusListener(this);

        final boolean gpsEnabled = mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
        if (!gpsEnabled) {
            Log.e(TAG, "GPSが無効です");
        } else {
            Log.e(TAG, "GPSが有効です");
        }
        requestLocationUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location){
        //Called when the location has changed.
        Log.e(TAG, "onLocationChanged.");
        showLocation(location);
    }

    @Override
    public void onProviderDisabled(String provider){
        //Called when the provider is disabled by the user.
        Log.e(TAG, "onProviderDisabled.");
        Log.e(TAG, provider + "が無効になりました。");
        showProvider(provider);
    }

    @Override
    public void onProviderEnabled(String provider){
        //Called when the provider is enabled by the user.
        Log.e(TAG, "onProviderEnabled.");
        Log.e(TAG, provider + "が有効になりました。");
        showProvider(provider);
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            requestLocationUpdates();
        }else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            requestLocationUpdates();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
        //Called when the provider status changes.
        Log.e(TAG, "onStatusChanged.");
        showProvider(provider);
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                // if the provider is out of service, and this is not expected to change in the near future.
                Log.e(TAG, provider +"が圏外になっていて取得できません。");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                // if the provider is temporarily unavailable but is expected to be available shortly.
                Log.e(TAG, "一時的に" + provider + "が利用できません。もしかしたらすぐに利用できるようになるかもです。");
                break;
            case LocationProvider.AVAILABLE:
                // if the provider is currently available.
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    Log.e(TAG, provider + "が利用可能になりました。");
                    requestLocationUpdates();
                }else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                    Log.e(TAG, provider + "が利用可能になりました。");
                    requestLocationUpdates();
                }
                break;
        }
    }

    public void onGpsStatusChanged( int event ){
        Iterable<GpsSatellite> satellites = mLocationManager.getGpsStatus(null).getSatellites();
        int sat_used = 0;
        int sat_total = 0; // GpsStaus.getMaxSatellites() の返り値が謎
        String str = "PRN : Azimuth : Elevation : S/N\n";
        for( GpsSatellite sat : satellites ) {
            if( sat.usedInFix()) {
                str += "*"; sat_total++; sat_used++;
            } else {
                str += " "; sat_total++;
            }
            str += String.format( "%2d", (int) sat.getPrn()) + " : ";
            str += String.format( "%7d", (int) sat.getAzimuth()) + " : ";
            str += String.format( "%9d", (int) sat.getElevation()) + " : ";
            str += String.format( "%4.1f", sat.getSnr()) + "\n";
        }
        str += String.format( "\nsatellites %d/%d\n" , sat_used, sat_total );

        Log.e(TAG,str);

        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            showLocation(location);
        }

        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        Log.e(TAG, "requestLocationUpdates()");
        showProvider(LocationManager.GPS_PROVIDER);
        showProvider(LocationManager.NETWORK_PROVIDER);
        boolean isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGpsEnabled){
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME,
                    LOCATION_UPDATE_MIN_DISTANCE,
                    this);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                showLocation(location);
            }
        }else if (isNetworkEnabled) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME,
                    LOCATION_UPDATE_MIN_DISTANCE,
                    this);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                showLocation(location);
            }
        } else {
            Log.e(TAG, "Networkが無効になっています。");
        }
    }

    private void showLocation(Location location) {
        Log.e(TAG, "showLocation()");
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        long time = location.getTime();
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
        String dateFormatted = formatter.format(date);
        Log.e(TAG, "Longitude : " + String.valueOf(longitude));
        Log.e(TAG, "Latitude : " + String.valueOf(latitude));
        Log.e(TAG, "取得時間 : " + dateFormatted);
    }

    private void showProvider(String provider){
        Log.e(TAG, "showProvider : " + provider);
    }

}
