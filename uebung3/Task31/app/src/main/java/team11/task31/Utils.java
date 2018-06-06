package team11.task31;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.view.Gravity;
import android.widget.Toast;

public class Utils {

    // Eddystone service uuid (0xfeaa)
    static final ParcelUuid UID_SERVICE =
            ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb");

    // Default namespace id for KST Eddystone beacons (d89bed6e130ee5cf1ba1)
    static final byte[] NAMESPACE_FILTER = {
            0x00, //Frame type
            0x00, //TX power
            (byte)0x21, (byte)0x83, (byte)0xa8, (byte)0x77, (byte)0xa7,
            (byte)0xb7, (byte)0x5d, (byte)0x07, (byte)0x15, (byte)0x13,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    // Force frame type and namespace id to match
    static final byte[] NAMESPACE_FILTER_MASK = {
            (byte)0xFF,
            0x00,
            (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
            (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    static final byte[] URL_FILTER = {
            0x10, //Frame type
            0x00, //TX power
            0x03, //URL prefix
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00
    };

    // Force frame type and protocol to match
    static final byte[] URL_FILTER_MASK = {
            (byte)0xFF,
            0x00,
            0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00
    };

    static final byte[] TLM_FILTER = {
            0x20, //Frame type
            0x00, //Protocol version = 0
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00
    };

    // Force frame type and protocol to match
    static final byte[] TLM_FILTER_MASK = {
            (byte)0xFF,
            (byte)0xFF,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00
    };

    // Eddystone frame types
    static final byte TYPE_UID = 0x00;
    static final byte TYPE_URL = 0x10;
    static final byte TYPE_TLM = 0x20;


    public static boolean checkBluetooth(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return false;
        }
        else {
            return true;
        }
    }

    public static void requestUserBluetooth(Activity activity) {
        // Request user permission for activating bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, MainActivity.REQUEST_ENABLE_BT);
    }

    public static void toast(Context context, String string) {
        Toast toast = Toast.makeText(context, string, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    public static String hexToString(byte[] data) {
        final StringBuilder sb = new StringBuilder(data.length);

        for(byte byteChar : data) {
            sb.append(String.format("%02X ", byteChar));
        }

        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 8),
                (byte)value};
    }

    public static double RssiToDistance(int Rssi, int a) {
        //return ((double)(a - Rssi))/100.0;
        a = -60;
        return Math.pow(10,((double)(Rssi - a))/-20.0);
    }
}
