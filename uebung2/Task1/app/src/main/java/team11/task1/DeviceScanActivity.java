package team11.task1;

import android.app.ListActivity;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.SyncStateContract;
import android.util.Log;

import java.util.*;


/**
 * Activity for scanning and displaying available BLE devices.
 */
public class DeviceScanActivity extends ListActivity {

    private BluetoothLeScanner mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    private boolean mScanning;
    private Handler mHandler;
    //private LeDeviceListAdapter mLeDeviceListAdapter;
    private UUID uuid = UUID.fromString("00000002-0000-0000-FDFD-FDFDFDFDFDFD");
    private ParcelUuid PUUID = new ParcelUuid(uuid);

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public void scanLeDevice() {
        List<ScanFilter> filters = getScanFilters(PUUID);
        ScanSettings settings = new ScanSettings.Builder().build();
        //mBluetoothAdapter.startScan(filters, settings, mLeScanCallback);
        mBluetoothAdapter.startScan(mLeScanCallback);
    }

    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackTypte, ScanResult result){
            super.onScanResult(callbackTypte, result);
            Log.e("42", result.toString());
        }
    };

    private List<ScanFilter> getScanFilters(ParcelUuid PUUID) {
        return Arrays.asList(
                new ScanFilter[]{
                        new ScanFilter.Builder()
                                .setServiceUuid(PUUID)
                                .build()
                }
        );
    }

}