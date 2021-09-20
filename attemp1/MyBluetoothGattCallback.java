package com.example.attemp1;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MyBluetoothGattCallback extends BluetoothGattCallback {
    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt,
                                        final int status,
                                        final int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
        } else {
            gatt.close();
        }
    }
}
