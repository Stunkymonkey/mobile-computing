package team11.task1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static team11.task1.MainActivity.REQUEST_ENABLE_BT;

public class Scanner_BTLE {

    private MainActivity ma;

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;
    private String TAG = Scanner_BTLE.class.getCanonicalName();
    private long scanPeriod;
    private int signalStrength;

    private UUID WEATHER_UUID = UUID.fromString("00000002-0000-0000-FDFD-FDFDFDFDFDFD");
    private ParcelUuid PUUID = new ParcelUuid(WEATHER_UUID);

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

    public boolean isScanning() {
        return mScanning;
    }

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
            List<ScanFilter> filters = getScanFilters(PUUID);
            ScanSettings settings = new ScanSettings.Builder().build();
            bluetoothLeScanner.startScan(scanCallback);
        }
        else {
            mScanning = false;
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    private List<ScanFilter> getScanFilters(ParcelUuid PUUID) {
        return Collections.singletonList(
                new ScanFilter.Builder()
                        .setServiceUuid(PUUID)
                        .build());
    }

    private ScanCallback scanCallback =
            new ScanCallback() {
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
                        Log.i(TAG, "result: " + result);
                    }
                }
            };

    public UUID getWEATHER_UUID() {
        return WEATHER_UUID;
    }
}