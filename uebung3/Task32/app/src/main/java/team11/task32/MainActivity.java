package team11.task32;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements ServiceConnection{

    private static final String TAG = "Task3.2";
    Intent i;

    private IGeoLogService geoLogServiceProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        i = new Intent(getApplicationContext(), GeoLogService.class);
        getApplicationContext().bindService(i, this, BIND_AUTO_CREATE);

        final Button button_start = findViewById(R.id.button_start);
        final Button button_stop = findViewById(R.id.button_stop);
        final Button button_update = findViewById(R.id.button_update);
        final Button button_exit = findViewById(R.id.button_exit);

        final TextView text_latitude = findViewById(R.id.text_latitude);
        final TextView text_longitude = findViewById(R.id.text_longitude);
        final TextView text_distance = findViewById(R.id.text_distance);
        final TextView text_average_speed = findViewById(R.id.text_average_speed);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "start");
                MainActivity.this.startService(i);
            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "stop");
                if (i != null) {
                    MainActivity.this.stopService(i);
                }
            }
        });

        button_update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "update");

                double r;

                assert(geoLogServiceProxy != null);
                try {
                    r = geoLogServiceProxy.getLatitude();
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                text_latitude.setText("Latitude: " + r);
                try {
                    r = geoLogServiceProxy.getLongitude();
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                text_longitude.setText("Longitude: " + r);
                try {
                    r = geoLogServiceProxy.getDistance();
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                text_distance.setText("Distance: " + r);
                try {
                    r = geoLogServiceProxy.getAverageSpeed();
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                text_average_speed.setText("Average Speed: " + r);

            }
        });

        button_exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "exit");
                System.exit(0);
            }
        });
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service){
        Log.i(TAG, "Service connected");
        geoLogServiceProxy = IGeoLogService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        geoLogServiceProxy = null;
    }

}
