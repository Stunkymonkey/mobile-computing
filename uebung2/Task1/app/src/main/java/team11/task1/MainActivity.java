package team11.task1;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static String LOG_TAG = MainActivity.class.getCanonicalName();
    public static int REQUEST_ENABLE_BT = 42; // Any positive integer should work.
    private BluetoothAdapter mBluetoothAdapter;
    private String uuid = "8e2e2964-e2f2-4d7e-a128-3e9f03ef6de7";
    private Button btn_Scan;
    private Scanner_BTLE mBTLeScanner;
    private HashMap<String, BTLE_Device> mBTDevicesHashMap;
    private ArrayList<BTLE_Device> mBTDevicesArrayList;
    private ArrayList<BluetoothGattCharacteristic> mGattCharacteristics;
    private ListAdapter_BTLE_Devices adapter;
    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BluetoothGatt mGatt;
    private SeekBar seek_bar;
    private final String T_UUID = "00002A1C-0000-1000-8000-00805F9B34FB";
    private final String H_UUID = "00002A6F-0000-1000-8000-00805F9B34FB";


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(LOG_TAG, "Connected to server!");
            //Utils.toast(getApplicationContext(), "Connected!");
            gatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //BluetoothGattCharacteristic gattChar = gatt.getServices().get(0).getCharacteristics().get(0);
            BluetoothGattCharacteristic gattCharTemp = gatt.getService(mBTLeScanner.getWEATHER_UUID()).getCharacteristic(UUID.fromString(T_UUID));
            BluetoothGattCharacteristic gattCharHum = gatt.getService(mBTLeScanner.getWEATHER_UUID()).getCharacteristic(UUID.fromString(H_UUID));
            boolean bla = gatt.readCharacteristic(gattCharTemp);
            boolean bla2 = gatt.readCharacteristic(gattCharHum);
            Log.i(LOG_TAG, "Temp boolean "+String.valueOf(bla));
            Log.i(LOG_TAG, "Hum boolean "+String.valueOf(bla2));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            Log.i(LOG_TAG, String.valueOf(status));
            int mantisse = (((int) characteristic.getValue()[3]) << 16) + (((int) characteristic.getValue()[2]) << 8)  + ((int) characteristic.getValue()[1]);
            float exponent = (float) characteristic.getValue()[4];
            float value = (float) (mantisse * Math.pow(10.0, exponent));

            Log.i(LOG_TAG, String.valueOf(value));
            Log.i(LOG_TAG, Arrays.toString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Utils.toast(getApplicationContext(), "Char value: " + characteristic.getValue());
        }
    };

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //Utils.toast(getApplicationContext(), "BLE not supported");
            finish();
        }

        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        mBTLeScanner = new Scanner_BTLE(this, 7500, -150);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();


        adapter = new ListAdapter_BTLE_Devices(this, R.layout.btle_device_list_item, mBTDevicesArrayList);

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        btn_Scan = (Button) findViewById(R.id.btn_scan);
        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);
        findViewById(R.id.btn_scan).setOnClickListener(this);

        seek_bar = (SeekBar) findViewById(R.id.seekBar1);
        ((SeekBar) findViewById(R.id.seekBar1)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(LOG_TAG, String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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

        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
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

        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
            } else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
    }

    /**
     * Adds a device to the ArrayList and Hashmap that the ListAdapter is keeping track of.
     *
     * @param device the BluetoothDevice to be added
     * @param rssi   the rssi of the BluetoothDevice
     */
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
        //Utils.toast(getApplicationContext(), "Item clicked at "+position+" "+id);
        Log.i(LOG_TAG, String.valueOf(view.getId()));
        BTLE_Device device = mBTDevicesArrayList.get(position);
        //Utils.toast(getApplicationContext(), device.getName());
        mGatt = device.getBluetoothDevice().connectGatt(getApplicationContext(), false,
                mGattCallback);
        /*
        if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic =
                    mGattCharacteristics.get(position);
            if (mBTLeScanner.getWEATHER_UUID().equals(characteristic.getUuid())) {
            }
        }

        Log.i(LOG_TAG, mGattCharacteristics.toString());
        */
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_scan:
                //Utils.toast(getApplicationContext(), "Scan Button Pressed");

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
        mGattCharacteristics.clear();


        adapter.notifyDataSetChanged();

        mBTLeScanner.start();
    }

    /**
     * Stops Scanner_BTLE
     * Changes the scan button text.
     */
    public void stopScan() {
        btn_Scan.setText("Scan Again");

        mBTLeScanner.stop();
    }
}
