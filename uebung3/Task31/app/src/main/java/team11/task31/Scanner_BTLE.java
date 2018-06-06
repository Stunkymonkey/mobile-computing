package team11.task31;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static team11.task31.Utils.NAMESPACE_FILTER;
import static team11.task31.Utils.NAMESPACE_FILTER_MASK;
import static team11.task31.Utils.TLM_FILTER;
import static team11.task31.Utils.TLM_FILTER_MASK;
import static team11.task31.Utils.TYPE_TLM;
import static team11.task31.Utils.TYPE_UID;
import static team11.task31.Utils.TYPE_URL;
import static team11.task31.Utils.UID_SERVICE;
import static team11.task31.MainActivity.REQUEST_ENABLE_BT;
import static team11.task31.Utils.URL_FILTER;
import static team11.task31.Utils.URL_FILTER_MASK;

public class Scanner_BTLE {

    private MainActivity ma;

    private static String LOG_TAG = MainActivity.class.getCanonicalName();
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;
    private String TAG = Scanner_BTLE.class.getCanonicalName();
    private long scanPeriod;
    private int signalStrength;

    public interface OnBeaconEventListener {
        void onBeaconIdentifier(String deviceAddress, int rssi, String instanceId);
        void onBeaconUrl(String deviceAddress);
        void onBeaconTelemetry(String deviceAddress, float battery, float temperature);
    }
    private OnBeaconEventListener mBeaconEventListener;


    public Scanner_BTLE(MainActivity mainActivity, long scanPeriod, int signalStrength) {
        ma = mainActivity;

        mHandler = new Handler();

        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) ma.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ma.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    public boolean isScanning() {return mScanning;}

    public void start() {
        if (!Utils.checkBluetooth(btAdapter)) {
            Utils.requestUserBluetooth(ma);
            ma.stopScan();
        }
        else {
            scanLeDevice(true);
        }
    }

    public void stop() {
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable && !mScanning) {
            Log.i(TAG, "started BLE scan");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(scanCallback);
                    ma.stopScan();
                }
            }, scanPeriod);

            mScanning = true;
            ScanFilter beaconFilter = new ScanFilter.Builder()
                    .setServiceUuid(UID_SERVICE)
                    .setServiceData(UID_SERVICE, NAMESPACE_FILTER, NAMESPACE_FILTER_MASK)
                    .build();

            ScanFilter urlFilter = new ScanFilter.Builder()
                    .setServiceUuid(UID_SERVICE)
                    .setServiceData(UID_SERVICE, URL_FILTER, URL_FILTER_MASK)
                    .build();

            ScanFilter telemetryFilter = new ScanFilter.Builder()
                    .setServiceUuid(UID_SERVICE)
                    .setServiceData(UID_SERVICE, TLM_FILTER, TLM_FILTER_MASK)
                    .build();

            List<ScanFilter> filters = new ArrayList<>();
            filters.add(beaconFilter);
            filters.add(urlFilter);
            filters.add(telemetryFilter);

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            //bluetoothLeScanner.startScan(scanCallback);
            bluetoothLeScanner.startScan(filters, settings, scanCallback);
        }
        else {
            mScanning = false;
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    /* Handle UID packet discovery on the main thread */
    private void processUidPacket(String deviceAddress, int rssi, String id, int tx_power) {
        Log.i(LOG_TAG, "Processed Uid packet: "+rssi+", "+id+", "+tx_power);
        ma.text_distance.setText("Distance: "+Utils.RssiToDistance(rssi, tx_power)+"m");
        ma.text_beaconid.setText("BeaconID: "+id);
        if (mBeaconEventListener != null) {
            mBeaconEventListener
                    .onBeaconIdentifier(deviceAddress, rssi, id);
        }
    }

    /* Handle TLM packet discovery on the main thread */
    private void processUrlPacket(String deviceAddress, String prefix, String url) {
        Log.i(LOG_TAG, "Processed Url packet: "+prefix+url);
        ma.text_url.setText("URL: "+prefix+url);
        if (mBeaconEventListener != null) {
            mBeaconEventListener
                    .onBeaconUrl(deviceAddress);
        }
    }

    /* Handle TLM packet discovery on the main thread */
    private void processTlmPacket(String deviceAddress, float battery, float temp) {
        Log.i(LOG_TAG, "Processed Tlm packet: "+battery+", "+temp);
        ma.text_voltage.setText("Voltage: "+battery+"V");
        ma.text_temperature.setText("Temperature: "+temp+"°C");
        if (mBeaconEventListener != null) {
            mBeaconEventListener
                    .onBeaconTelemetry(deviceAddress, battery, temp);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {

            private Handler mCallbackHandler =
                    new Handler(Looper.getMainLooper());

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                final ScanResult r = result;
                if (r.getRssi() > signalStrength) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ma.addDevice(r.getDevice(), r.getRssi());
                        }
                    });
                    //Log.i(TAG, "result: " + result);
                }
                byte[] data = result.getScanRecord().getServiceData(UID_SERVICE);
                if (data == null) {
                    Log.w(TAG, "Invalid Eddystone scan result.");
                    return;
                }

                final String deviceAddress = result.getDevice().getAddress();
                final int rssi = result.getRssi();
                byte frameType = data[0];
                switch (frameType) {
                    case TYPE_UID:
                        final String id = SampleBeacon.getInstanceId(data);
                        final int tx_power = SampleBeacon.getTxPower(data);
                        mCallbackHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                processUidPacket(deviceAddress, rssi, id, tx_power);
                            }
                        });
                        break;
                    case TYPE_TLM:
                        //Parse out battery voltage
                        final float battery = SampleBeacon.getTlmBattery(data);
                        final float temp = SampleBeacon.getTlmTemperature(data);
                        mCallbackHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                processTlmPacket(deviceAddress, battery, temp);
                            }
                        });
                        break;
                    case TYPE_URL:
                        final String urlprefix = SampleBeacon.getUrlPrefix(data);
                        final String url = SampleBeacon.getUrl(data);
                        processUrlPacket(deviceAddress, urlprefix, url);
                        return;
                    default:
                        Log.w(TAG, "Invalid Eddystone scan result.");
                }
            }
        };
}