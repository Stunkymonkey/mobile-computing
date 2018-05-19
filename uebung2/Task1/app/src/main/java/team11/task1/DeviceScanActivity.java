package team11.task1;

import android.app.ListActivity;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.*;


/**
 * Activity for scanning and displaying available BLE devices.
 */
public class DeviceScanActivity extends ListActivity {

    private BluetoothAdapter BluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    BluetoothDevice myDevice;
    BluetoothGatt gatt;
    private BluetoothLeScanner mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    private String TAG = DeviceScanActivity.class.getCanonicalName();
    //private LeDeviceListAdapter mLeDeviceListAdapter;
    private String WEATHER_ADDRESS;
    private UUID WEATHER_UUID = UUID.fromString("00000002-0000-0000-FDFD-FDFDFDFDFDFD");
    private ParcelUuid PUUID = new ParcelUuid(WEATHER_UUID);

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    // look here: https://medium.com/@avigezerit/bluetooth-low-energy-on-android-22bc7310387a
    BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getAddress().equals(WEATHER_ADDRESS)) {
                myDevice = device;
            }

        }
    };
    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            //gatt = myDevice.connectGatt(this,true,gattCallback);
        }

    };

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            List<ScanFilter> filters = getScanFilters(PUUID);
            ScanSettings settings = new ScanSettings.Builder().build();
            //mBluetoothAdapter.startScan(filters, settings, mLeScanCallback);
            mBluetoothAdapter.startScan(mLeScanCallback);

        } else {
            mScanning = false;
            mBluetoothAdapter.stopScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackTypte, ScanResult result) {
            super.onScanResult(callbackTypte, result);
            Log.i(TAG, result.toString());
        }
    };

    private List<ScanFilter> getScanFilters(ParcelUuid PUUID) {
        return Collections.singletonList(
                new ScanFilter.Builder()
                        .setServiceUuid(PUUID)
                        .build());
    }


}