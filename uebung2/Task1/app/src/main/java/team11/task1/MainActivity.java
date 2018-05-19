package team11.task1;

import android.app.ProgressDialog;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import static android.provider.Settings.Global.DEVICE_NAME;

public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG = "MC-Task1";
    private int REQUEST_ENABLE_BT = 42; // Any positive integer should work.
    private BluetoothAdapter mBluetoothAdapter;
    private String uuid = "8e2e2964-e2f2-4d7e-a128-3e9f03ef6de7";

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LOG_TAG = getResources().getString(R.string.app_name);
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) throw new AssertionError();
        mBluetoothAdapter = bluetoothManager.getAdapter();

    }

    public void scan(View view){
        enableBluetoothOnDevice();
        DeviceScanActivity dsa = new DeviceScanActivity();
        dsa.scanLeDevice(true);

    }

    private void enableBluetoothOnDevice()
    {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLe not supprted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT)
        {
            if (resultCode == 0)
            {
                // If the resultCode is 0, the user selected "No" when prompt to
                // allow the app to enable bluetooth.
                // You may want to display a dialog explaining what would happen if
                // the user doesn't enable bluetooth.
                Toast.makeText(this, "The user decided to deny bluetooth access", Toast.LENGTH_LONG).show();
                //System.exit(1);
            }
            else
                Log.i(LOG_TAG, "User allowed bluetooth access!");
        }
    }

}
