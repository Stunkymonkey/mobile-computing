package team11.task32;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Task3.2";
    Intent GeoLogService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GeoLogService = new Intent(MainActivity.this, GeoLogService.class);

        final Button button_start = findViewById(R.id.button_start);
        final Button button_stop = findViewById(R.id.button_stop);
        final Button button_update = findViewById(R.id.button_update);
        final Button button_exit = findViewById(R.id.button_exit);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "start");
                MainActivity.this.startService(GeoLogService);
            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "stop");
                if (GeoLogService != null) {
                    MainActivity.this.stopService(GeoLogService);
                }
            }
        });

        button_update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "update");
            }
        });

        button_exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "exit");
            }
        });
    }
}
