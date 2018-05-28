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
    private Button btn_temp;
    private Button btn_hum;
    private Scanner_BTLE mBTLeScanner;
    private HashMap<String, BTLE_Device> mBTDevicesHashMap;
    private ArrayList<BTLE_Device> mBTDevicesArrayList;
    private ArrayList<BluetoothGattCharacteristic> mGattCharacteristics;
    private ListAdapter_BTLE_Devices adapter;
    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BluetoothGatt mGatt;
    private SeekBar seek_bar;
    private boolean read_temp;
    private final String T_UUID = "00002A1C-0000-1000-8000-00805F9B34FB";
    private final String H_UUID = "00002A6F-0000-1000-8000-00805F9B34FB";
    private final String I_UUID = "10000001-0000-0000-FDFD-FDFDFDFDFDFD";
    private final String FAN_UUID = "00000001-0000-0000-FDFD-FDFDFDFDFDFD";

    private float getTemperatureFromBytes(byte[] bytes) {
        int mantisse = (((int) bytes[3]) << 16) + (((int) bytes[2]) << 8)  + ((int) bytes[1]);
        float exponent = (float) bytes[4];
        float value = (float) (mantisse * Math.pow(10.0, exponent));
        return value;
    }

    private float getHumidityFromBytes(byte[] bytes) {
        float value = (((int) bytes[1]) << 8) + ((int) bytes[0]);
        return value / 100.0f;
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(LOG_TAG, "Connected to server!");
            //Utils.toast(getApplicationContext(), "Connected!");
            gatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            if (read_temp) {
                float value = getTemperatureFromBytes(characteristic.getValue());
                Log.i(LOG_TAG, String.valueOf(value));
                ((Button)findViewById(R.id.button_temp)).setText("Temp: "+String.valueOf(value)+"°C");
            } else {
                float value = getHumidityFromBytes(characteristic.getValue());
                Log.i(LOG_TAG, String.valueOf(value));
                ((Button)findViewById(R.id.button_hum)).setText("Hum: "+String.valueOf(value)+"%");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(UUID.fromString(T_UUID))) {
                float value = getTemperatureFromBytes(characteristic.getValue());
                ((Button)findViewById(R.id.button_temp)).setText("Temp: "+String.valueOf(value)+"°C");
                Log.i(LOG_TAG, "Characteristic "+characteristic.getUuid()+ " has changed to " + value);
            } else if (characteristic.getUuid().equals(UUID.fromString(H_UUID))){
                float value = getHumidityFromBytes(characteristic.getValue());
                ((Button)findViewById(R.id.button_hum)).setText("Hum: "+String.valueOf(value)+"%");
                Log.i(LOG_TAG, "Characteristic "+characteristic.getUuid()+ " has changed to " + value);
            } else {
                Log.i(LOG_TAG, "Something wrong here! " + characteristic.getUuid());
            }
        }


        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(LOG_TAG, "Writing characteristic: "+characteristic.getUuid());
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

        btn_temp = (Button) findViewById(R.id.button_temp);
        ((Button) findViewById(R.id.button_temp)).setOnClickListener(this);

        btn_hum = (Button) findViewById(R.id.button_hum);
        ((Button) findViewById(R.id.button_hum)).setOnClickListener(this);

        seek_bar = (SeekBar) findViewById(R.id.seekBar1);
        ((SeekBar) findViewById(R.id.seekBar1)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Log.i(LOG_TAG, String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int seekbar_value = seekBar.getProgress();
                BluetoothGattCharacteristic gattCharInt = mGatt.getService(UUID.fromString(FAN_UUID)).getCharacteristic(UUID.fromString(I_UUID));
                gattCharInt.setValue(Utils.intToByteArray(seekbar_value));
                mGatt.writeCharacteristic(gattCharInt);
                Log.i(LOG_TAG, "New seekbar value: "+seekBar.getProgress());
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
            case R.id.button_temp:
                read_temp = true;
                BluetoothGattCharacteristic gattCharTemp = mGatt.getService(mBTLeScanner.getWEATHER_UUID()).getCharacteristic(UUID.fromString(T_UUID));
                mGatt.setCharacteristicNotification(gattCharTemp, true);
                Log.i(LOG_TAG, gattCharTemp.getDescriptors().get(0).getUuid().toString());
                BluetoothGattDescriptor t_descriptor = gattCharTemp.getDescriptor(UUID.fromString("00002904-0000-1000-8000-00805f9b34fb"));
                if (t_descriptor != null) {
                    t_descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mGatt.writeDescriptor(t_descriptor);
                }
                mGatt.readCharacteristic(gattCharTemp);
                break;
            case R.id.button_hum:
                read_temp = false;
                BluetoothGattCharacteristic gattCharHum = mGatt.getService(mBTLeScanner.getWEATHER_UUID()).getCharacteristic(UUID.fromString(H_UUID));
                mGatt.setCharacteristicNotification(gattCharHum, true);
                Log.i(LOG_TAG, gattCharHum.getDescriptors().get(0).getUuid().toString());
                BluetoothGattDescriptor h_descriptor = gattCharHum.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                if (h_descriptor != null) {
                    h_descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mGatt.writeDescriptor(h_descriptor);
                }
                mGatt.readCharacteristic(gattCharHum);
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
