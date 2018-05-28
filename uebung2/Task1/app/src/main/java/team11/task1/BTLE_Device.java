package team11.task1;

import android.bluetooth.BluetoothDevice;

public class BTLE_Device {
    private BluetoothDevice bluetoothDevice;
    private int rssi;

    public BTLE_Device(BluetoothDevice bluetoothDevice) {this.bluetoothDevice = bluetoothDevice;}

    public String getAddress() {return bluetoothDevice.getAddress();}

    public String getName() {return bluetoothDevice.getName();}

    public int getRSSI() {return rssi;}

    public void setRSSI(int rssi) {
        this.rssi = rssi;
    }

    public BluetoothDevice getBluetoothDevice() {return this.bluetoothDevice;}
}
