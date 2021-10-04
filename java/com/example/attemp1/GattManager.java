package com.example.attemp1;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.icu.lang.UCharacter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GattManager {
    public static BluetoothGatt gatt = null;
    private MyBluetoothGattCallback callback = new MyBluetoothGattCallback();
    private static Queue<Runnable> commandQueue;
    private static boolean commandQueueBusy;
    private static int nrTries;
    private static boolean isRetrying;
    public static BluetoothGattService serviceToWrite = new BluetoothGattService(UUID.randomUUID(), BluetoothGattService.SERVICE_TYPE_PRIMARY);
    public static BluetoothGattCharacteristic characteristicToWrite = new BluetoothGattCharacteristic(UUID.randomUUID(),0,0);
    private static Handler bleHandler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.M)
    public GattManager(BluetoothDevice device){
        this.gatt = device.connectGatt(MainActivity.listViewPairedDevice.getContext(), false, callback, BluetoothDevice.TRANSPORT_LE);
    }

    public static boolean haveGatt(){
        if (gatt != null)
            return true;
        else
            return false;
    }

    public static boolean readCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (gatt == null) {
            Log.e(TAG, "ERROR: Gatt is 'null', ignoring read request");
            return false;
        }

        // Check if characteristic is valid
        if (characteristic == null) {
            Log.e(TAG, "ERROR: Characteristic is 'null', ignoring read request");
            return false;
        }

        // Check if this characteristic actually has READ property
        if ((characteristic.getProperties() & PROPERTY_READ) == 0) {
            Log.e(TAG, "ERROR: Characteristic cannot be read");
            return false;
        }

        // Enqueue the read command now that all checks have been passed
        boolean result = commandQueue.add(new Runnable() {
            @Override
            public void run() {
                if (!gatt.readCharacteristic(characteristic)) {
                    Log.e(TAG, String.format("ERROR: readCharacteristic failed for characteristic: %s", characteristic.getUuid()));
                    completedCommand();
                } else {
                    Log.d(TAG, String.format("reading characteristic <%s>", characteristic.getUuid()));
                    nrTries++;
                }
            }
        });

        if (result) {
            nextCommand();
        } else {
            Log.e(TAG, "ERROR: Could not enqueue read characteristic command");
        }
        return result;
    }

    private static void nextCommand() {
        // If there is still a command being executed then bail out
        if (commandQueueBusy) {
            return;
        }

        // Check if we still have a valid gatt object
        if (gatt == null) {
            Log.e(TAG, String.format("ERROR: GATT is 'null' for peripheral '%s', clearing command queue", MainActivity.bluetoothAdapter.getAddress()));
            commandQueue.clear();
            commandQueueBusy = false;
            return;
        }

        // Execute the next command in the queue
        if (commandQueue.size() > 0) {
            final Runnable bluetoothCommand = commandQueue.peek();
            commandQueueBusy = true;
            nrTries = 0;


            bleHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        bluetoothCommand.run();
                    } catch (Exception ex) {
                        Log.e(TAG, "ERROR: Command exception for device", ex);
                    }
                }
            });
        }
    }

    private static void completedCommand() {
        commandQueueBusy = false;
        isRetrying = false;
        commandQueue.poll();
        nextCommand();
    }

    private void retryCommand() {
        commandQueueBusy = false;
        Runnable currentCommand = commandQueue.peek();
        if (currentCommand != null) {
            if (nrTries >= 3) {
                // Max retries reached, give up on this one and proceed
                Log.v(TAG, "Max number of tries reached");
                commandQueue.poll();
            } else {
                isRetrying = true;
            }
        }
        nextCommand();
    }

    public static void writeCharacteristic(byte[] bytesToWrite, BluetoothGattCharacteristic characteristic) {
        characteristic.setValue(bytesToWrite);
        characteristic.setWriteType(WRITE_TYPE_DEFAULT);
        if (!gatt.writeCharacteristic(characteristic)) {
            Log.e(TAG, String.format("ERROR: writeCharacteristic failed for characteristic: %s", characteristic.getUuid()));
            completedCommand();
        } else {
            Log.d(TAG, String.format("writing <%s> to characteristic <%s>", new String(bytesToWrite), characteristic.getUuid()));
            nrTries++;
        }
    }
}
