package com.example.attemp1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import static android.R.layout.simple_list_item_1;
import static android.R.layout.simple_list_item_single_choice;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    ToggleButton sen_tb;
    private static final int REQUEST_ENABLE_BT = 1;
    public TextView textInfo;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<String> pairedDeviceList;
    ListView listViewPairedDevice;
    FrameLayout ButPanel;
    ArrayAdapter<String> pairedDeviceAdapter;
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    private UUID myUUID;
    private StringBuilder sb = new StringBuilder();
    private Handler bleHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sen_tb = (ToggleButton) findViewById(R.id.toggleButton1);
        sen_tb.setOnCheckedChangeListener(this);

        final String UUID_SENSOR = "00001101-0000-1000-8000-0805F9B34FB";
        textInfo = (TextView) findViewById(R.id.textInfo);
        listViewPairedDevice = (ListView) findViewById(R.id.list);
        ButPanel = (FrameLayout) findViewById(R.id.panel);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "Bluetooth is not support", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        myUUID = UUID.fromString(UUID_SENSOR);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String Info = bluetoothAdapter.getName() + " " + bluetoothAdapter.getAddress();
        textInfo.setText(String.format("This device: %s", Info));
    }

    @Override
    protected void onStart() { // Запрос на включение Bluetooth
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        setup();
    }

    private void setup() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDeviceList = new ArrayList<>();
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceList.add(device.getName() + "\n" + device.getAddress());
            }
            pairedDeviceAdapter = new ArrayAdapter<>(this, simple_list_item_1, pairedDeviceList);
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);
            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    listViewPairedDevice.setVisibility(View.GONE);
                    String itemValue = (String) listViewPairedDevice.getItemAtPosition(position);
                    String MAC = itemValue.substring(itemValue.length() - 17);
                    BluetoothDevice device2 = bluetoothAdapter.getRemoteDevice(MAC);
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device2);
                    myThreadConnectBTdevice.start();
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myThreadConnectBTdevice != null) myThreadConnectBTdevice.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                setup();
            }
        } else {
            Toast.makeText(this, "Try turn on Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.toggleButton1:
                if (isChecked) {
                    if (myThreadConnected != null) {
                        byte[] bytesToSend = "a".getBytes();
                        //myThreadConnected.write(bytesToSend);
                    }
                    Toast.makeText(MainActivity.this, "Sensor ON", Toast.LENGTH_SHORT).show();
                } else {
                    if (myThreadConnected != null) {
                        byte[] bytesToSend = "A".getBytes();
                        //myThreadConnected.write(bytesToSend);
                    }
                    Toast.makeText(MainActivity.this, "Sensor OFF", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /*public class ThreadConnectBTdevice extends Thread{
        private BluetoothSocket bluetoothSocket = null;
        private ThreadConnectBTdevice(BluetoothDevice device){
            try{
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try{
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "No connection. Check your sensor", Toast.LENGTH_LONG).show();
                        listViewPairedDevice.setVisibility(View.VISIBLE);
                    }
                });
                try {
                    bluetoothSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if(success){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ButPanel.setVisibility(View.VISIBLE);
                    }
                });
                myThreadConnected = new ThreadConnected(bluetoothSocket);
                myThreadConnected.start();
            }
        }
        public void cancel(){
            Toast.makeText(getApplicationContext(), "Close - BluetoothSocket", Toast.LENGTH_LONG ).show();
            try{
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public class ThreadConnectBTdevice extends Thread {
        private BluetoothGatt gatt = null;
        private MyBluetoothGattCallback callback = new MyBluetoothGattCallback();

        @RequiresApi(api = Build.VERSION_CODES.M)
        private ThreadConnectBTdevice(BluetoothDevice device) {
            gatt = device.connectGatt(getApplicationContext(), false, callback, BluetoothDevice.TRANSPORT_LE);
        }

        @Override
        public void run() {
            if (gatt != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ButPanel.setVisibility(View.VISIBLE);
                    }
                });
                myThreadConnected = new ThreadConnected(gatt, callback);
                myThreadConnected.start();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "No connection. Check your sensor", Toast.LENGTH_LONG).show();
                        listViewPairedDevice.setVisibility(View.VISIBLE);
                    }
                });
            }

        }

        public void cancel() {
            Toast.makeText(getApplicationContext(), "Close - GATT", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public class ThreadConnected extends Thread {
        private final BluetoothGatt gatt;
        private final BluetoothGattCallback callback;
        private Queue<Runnable> commandQueue;
        private boolean commandQueueBusy;
        private int nrTries;
        private boolean isRetrying;
        public ThreadConnected(BluetoothGatt gatt, BluetoothGattCallback callback) {
            this.gatt = gatt;
            this.callback = callback;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        public boolean readCharacteristic(final BluetoothGattCharacteristic characteristic) {
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

        private void nextCommand() {
            // If there is still a command being executed then bail out
            if (commandQueueBusy) {
                return;
            }

            // Check if we still have a valid gatt object
            if (gatt == null) {
                Log.e(TAG, String.format("ERROR: GATT is 'null' for peripheral '%s', clearing command queue", bluetoothAdapter.getAddress()));
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
                            Log.e(TAG, String.format("ERROR: Command exception for device '%s'", getName()), ex);
                        }
                    }
                });
            }
        }

        private void completedCommand() {
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

        public void write(byte[] bytesToWrite, BluetoothGattCharacteristic characteristic) {
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

        @Override
        public void run() {
            Looper.prepare();
            while (true) {
                if(gatt.getServices() != null){
                    Toast.makeText(MainActivity.this, "Services connected", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Servises not connected" , Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

