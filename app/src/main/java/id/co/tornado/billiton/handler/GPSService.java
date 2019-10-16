package id.co.tornado.billiton.handler;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import id.co.tornado.billiton.SocketService;
import id.co.tornado.billiton.module.listener.GPSListener;

/**
 * Created by Ahmad on 6/29/2016.
 */
public class GPSService implements Runnable {
    private GPSListener listener;
    private LocationManager locationManager;
    private Location location;
    private String longitude;
    private String latitude;
    private boolean running=false;
    private int counter;
    private SocketService context;

    public GPSService(SocketService context) {
        locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
//            Log.d("GPS","Setting Provider");
        } else {
//            Log.d("GPS", "Provider GPS Ready");
        }
        listener = new GPSListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, listener);
        longitude = "0";
        latitude = "0";
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateLocation(location);
        running = true;
        counter = 1;
        this.context = context;
    }

    @Override
    public void run() {
        //loop updateLocation
        while (running) {
//            Log.d("GPS", "#"+String.valueOf(counter)+" Update Location");
            counter++;
            location = listener.getGPSLocation();
            updateLocation(location);
            try {
                Thread.sleep(720000);
            } catch (InterruptedException e) {
                stop();
                e.printStackTrace();
            }
        }
    }

    private void updateLocation(Location location) {
        if (location!=null) {
            longitude = String.valueOf(location.getLongitude());
            latitude = String.valueOf(location.getLatitude());
            try {
                OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput("loc.txt",Context.MODE_PRIVATE));
                osw.write(longitude+","+latitude);
                osw.close();
            } catch (FileNotFoundException e) {
//                e.printStackTrace();
                Log.e("GPS", "Update Error : File not found");
            } catch (IOException e) {
//                e.printStackTrace();
                Log.e("GPS", "Update Error : Cannot access file");
            } catch (Exception e) {
                Log.e("GPS", "Update Error : Application not ready yet");
            }
//            Log.d("GPS", "SUPD : " + longitude + ", " + latitude);
        }
    }

    public void stop() {
        running = false;
    }

}
