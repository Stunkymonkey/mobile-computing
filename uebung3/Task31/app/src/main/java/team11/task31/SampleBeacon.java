package team11.task31;

import android.util.Log;

import java.text.NumberFormat;

import static java.lang.System.arraycopy;

/**
 * Simple model class to house relevant data coming from
 * beacon advertisements.
 */
public class SampleBeacon {
    private static final String TAG = SampleBeacon.class.getSimpleName();

    public String deviceAddress;
    public String id;
    public int latestRssi;
    public long lastDetectedTimestamp;

    public float battery;
    public float temperature;

    public SampleBeacon(String address, int rssi, String identifier, long timestamp) {
        this.deviceAddress = address;
        this.latestRssi = rssi;
        this.id = identifier;
        this.lastDetectedTimestamp = timestamp;

        this.battery = -1f;
        this.temperature = -1f;
    }

    public void update(String address, int rssi, long timestamp) {
        this.deviceAddress = address;
        this.latestRssi = rssi;
        this.lastDetectedTimestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("%s\n%ddBm    Battery: %s    Temperature: %s",
                id, latestRssi,
                battery < 0f ? "N/A" : String.format("%.1fV", battery),
                temperature < 0f ? "N/A" : String.format("%.1fC", temperature));
    }

    // Parse the instance id out of a UID packet
    public static String getInstanceId(byte[] data) {
        StringBuilder sb = new StringBuilder();

        //UID packets are always 18 bytes in length
        //Parse out the last 6 bytes for the id
        int packetLength = 18;
        int offset = packetLength - 6;
        for (int i=offset; i < packetLength; i++) {
            sb.append(Integer.toHexString(data[i] & 0xFF));
        }

        return sb.toString();
    }

    // Parse the battery level out of a TLM packet
    public static float getTlmBattery(byte[] data) {
        byte version = data[1];
        if (version != 0) {
            Log.w(TAG, "Unknown telemetry version");
            return -1;
        }
        int voltage = (data[2] & 0xFF) << 8;
        voltage += (data[3] & 0xFF);

        //Value is 1mV per bit
        return voltage / 1000f;
    }

    // Parse the temperature out of a TLM packet
    public static float getTlmTemperature(byte[] data) {
        byte version = data[1];
        if (version != 0) {
            Log.w(TAG, "Unknown telemetry version");
            return -1;
        }

        if (data[4] == (byte)0x80 && data[5] == (byte)0x00) {
            Log.w(TAG, "Temperature not supported");
            return -1;
        }

        int temp = (data[4] << 8);
        temp += (data[5] & 0xFF);

        return temp / 256f;
    }

    public static String getUrlPrefix(byte[] data){
        switch(data[2]) {
            case (byte)0x00:
                return "http://www.";
            case (byte) 0x01:
                return "https://www.";
            case (byte) 0x02:
                return "http://";
            case (byte) 0x03:
                return "https://";
            default:
                return "";
        }
    }

    public static String getUrl(byte[] data) {
        byte[] url = new byte[data.length-3];
        arraycopy(data, 3, url, 0, data.length-3);
        return new String(url);
    }

    public static int getTxPower(byte[] data) {
        return data[1];
    }
}
