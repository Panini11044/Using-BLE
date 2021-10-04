package com.example.attemp1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
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
import java.util.List;
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

import Command.AllCommand;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    ToggleButton sen_tb;
    private static final int REQUEST_ENABLE_BT = 1;
    public TextView textInfo;
    static BluetoothAdapter bluetoothAdapter;
    ArrayList<String> pairedDeviceList;
    static ListView listViewPairedDevice;
    static FrameLayout ButPanel;
    ArrayAdapter<String> pairedDeviceAdapter;
    public ThreadConnectBTdevice myThreadConnectBTdevice;
    private UUID myUUID;
    public static GattManager gattManager;

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
                    gattManager = new GattManager(device2);
                    myThreadConnectBTdevice = new ThreadConnectBTdevice();
                    myThreadConnectBTdevice.start();
                    if (GattManager.haveGatt()) {
                        MainActivity.ButPanel.setVisibility(View.VISIBLE);
                    }
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.toggleButton1:
                if (isChecked) {
                        GattManager.writeCharacteristic(AllCommand.readTime.makeMessage(), GattManager.characteristicToWrite);
                    Toast.makeText(MainActivity.this, "Send message to device", Toast.LENGTH_SHORT).show();
                } else {
                    if (GattManager.readCharacteristic(GattManager.characteristicToWrite)){
                        AllCommand.readTime.getMessage(GattManager.characteristicToWrite.getValue());
                        Toast.makeText(MainActivity.this, AllCommand.readTime.answer, Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(MainActivity.this, "Sensor OFF", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

