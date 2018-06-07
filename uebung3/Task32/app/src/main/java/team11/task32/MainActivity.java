package team11.task32;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements ServiceConnection{

    private static final String TAG = "Task3.2";
    Intent i;

    private IGeoLogService geoLogServiceProxy;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        i = new Intent(getApplicationContext(), GeoLogService.class);

        final Button button_start = findViewById(R.id.button_start);
        final Button button_stop = findViewById(R.id.button_stop);
        final Button button_update = findViewById(R.id.button_update);
        final Button button_exit = findViewById(R.id.button_exit);

        final TextView text_latitude = findViewById(R.id.text_latitude);
        final TextView text_longitude = findViewById(R.id.text_longitude);
        final TextView text_distance = findViewById(R.id.text_distance);
        final TextView text_average_speed = findViewById(R.id.text_average_speed);

        ActivityCompat.requestPermissions(this,permissions, 1);

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.i(TAG, "unable to write to file");
        }

        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "start");
                getApplicationContext().bindService(i, MainActivity.this, BIND_AUTO_CREATE);
            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "stop");
                getApplicationContext().unbindService(MainActivity.this);
            }
        });

        button_update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "update");

                DecimalFormat df = new DecimalFormat("#.00");
                DecimalFormat pf = new DecimalFormat("#.000000");
                double r;

                if(geoLogServiceProxy != null) {
                    try {
                        r = geoLogServiceProxy.getLatitude();
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    text_latitude.setText("Latitude: " + pf.format(r));
                    try {
                        r = geoLogServiceProxy.getLongitude();
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    text_longitude.setText("Longitude: " + pf.format(r));
                    try {
                        r = geoLogServiceProxy.getDistance();
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    text_distance.setText("Distance: " + df.format(r) + " m");
                    try {
                        r = geoLogServiceProxy.getAverageSpeed();
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    text_average_speed.setText("Average Speed: " + df.format(r) + " m/s");
                }
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
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service){
        Log.i(TAG, "Service connected");
        geoLogServiceProxy = IGeoLogService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "Service disconnected");
        geoLogServiceProxy = null;
    }

}
