package team11.task31;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static String LOG_TAG = MainActivity.class.getCanonicalName();
    public static int REQUEST_ENABLE_BT = 42; // Any positive integer should work.
    public TextView text_beaconid;
    public TextView text_url;
    public TextView text_voltage;
    public TextView text_temperature;
    public TextView text_distance;

    private Button btn_Scan;
    private Scanner_BTLE mBTLeScanner;
    private HashMap<String, BTLE_Device> mBTDevicesHashMap;
    private ArrayList<BTLE_Device> mBTDevicesArrayList;
    private ListAdapter_BTLE_Devices adapter;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //BLE not supported
            finish();
        }

        mBTLeScanner = new Scanner_BTLE(this, 7500, -150);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();


        adapter = new ListAdapter_BTLE_Devices(this, R.layout.btle_device_list_item, mBTDevicesArrayList);

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        btn_Scan = (Button) findViewById(R.id.btn_scan);
        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);
        ((Button) findViewById(R.id.btn_scan)).setOnClickListener(this);

        text_beaconid = findViewById(R.id.text_view_beaconid);
        text_url = findViewById(R.id.text_view_url);
        text_voltage = findViewById(R.id.text_view_voltage);
        text_temperature = findViewById(R.id.text_view_temperature);
        text_distance = findViewById(R.id.text_view_distance);

        // Location permission
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Bluetooth request
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
            } else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
    }

    public void addDevice(BluetoothDevice device, int rssi) {
        String address = device.getAddress();
        if (!mBTDevicesHashMap.containsKey(address)) {
            BTLE_Device btleDevice = new BTLE_Device(device);
            btleDevice.setRSSI(rssi);

            mBTDevicesHashMap.put(address, btleDevice);
            mBTDevicesArrayList.add(btleDevice);
        } else {
            mBTDevicesHashMap.get(address).setRSSI(rssi);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (!mBTLeScanner.isScanning()) {
                    startScan();
                } else {
                    stopScan();
                }
                break;
            default:
                break;
        }
    }

    public void startScan() {
        btn_Scan.setText("Scanning...");
        mBTDevicesArrayList.clear();
        mBTDevicesHashMap.clear();
        adapter.notifyDataSetChanged();
        mBTLeScanner.start();
    }

    public void stopScan() {
        btn_Scan.setText("Scan Again");
        mBTLeScanner.stop();
    }
}