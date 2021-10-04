package com.example.attemp1;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ThreadConnectBTdevice extends Thread {
    @Override
    public void run() {
        if (GattManager.haveGatt()) {
            GattManager.serviceToWrite = GattManager.gatt.getService(UUID.fromString("00001523-1212-efde-1523-785feabcd123"));
            GattManager.characteristicToWrite = GattManager.serviceToWrite.getCharacteristic(UUID.fromString("00001524-1212-efde-1523-785feabcd123"));

        }
        else {
            Toast.makeText(MainActivity.listViewPairedDevice.getContext(), "No connection. Check your sensor", Toast.LENGTH_LONG).show();
            MainActivity.listViewPairedDevice.setVisibility(View.VISIBLE);
        }
    }
    public void cancel() {
        Toast.makeText(MainActivity.listViewPairedDevice.getContext(), "Close - GATT", Toast.LENGTH_LONG).show();
    }
}
