package team11.task32;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;

public class GeoLogService extends Service {
    private static final String TAG = "GeoLogService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 5f;
    private Location mLastLocation;
    private double distance = 0;
    private boolean first = true;
    private Location firstLocation;

    private GeoLogServiceImpl impl;
    private GPX gpx;

    private class LocationListener implements android.location.LocationListener
    {
        public LocationListener(String provider) {
            Log.i(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
            first = true;
            distance = 0;
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged: " + location);
            if (first) {
                first = false;
                firstLocation = location;
            } else {
                distance += mLastLocation.distanceTo(location);
            }
            mLastLocation.set(location);
            if (gpx != null) {
                gpx.addPoint(location);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "onStatusChanged: " + provider + " status: " + status);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Binding Service");
        return impl;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting Service");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        Log.i(TAG, "Creating Service");
        impl = new GeoLogServiceImpl();
        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String filename = "mobile-computing.gpx"; //df.format(new Date(mLastLocation.getTime()));
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
        gpx = new GPX(f, filename);
        //Log.i(TAG, "file: " + f.getAbsolutePath() + filename);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroying Service");
        if (gpx != null) {
            gpx.close();
        }
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
            //mLocationManager = null;
        }
        super.onDestroy();
    }

    private void initializeLocationManager() {
        Log.i(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private class GeoLogServiceImpl extends IGeoLogService.Stub {

        @Override
        public double getLatitude() throws RemoteException {
            if (mLastLocation != null) {
                return mLastLocation.getLatitude();
            }
            return 0;
        }

        @Override
        public double getLongitude() throws RemoteException {
            if (mLastLocation != null) {
                return mLastLocation.getLongitude();
            }
            return 0;
        }

        @Override
        public double getDistance() throws RemoteException {
            return distance;
        }

        @Override
        public double getAverageSpeed() throws RemoteException {
            if (mLastLocation != null && firstLocation != null){
                double interval = (mLastLocation.getTime() - firstLocation.getTime()) / 1000;
                return distance / (interval);
            }
            return 0;
        }
    }
}