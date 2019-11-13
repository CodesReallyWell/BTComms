package com.example.btcomms;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;




public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT   = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView mStatusBlueTv, mPairedTv, mConnectionTv;
    ImageView mBlueIv;
    Button mOnBtn, mOffBtn, mDiscoveredBtn, mPairedBtn, mConnectBtn;

    // Bluetooth variables
    BluetoothAdapter mBlueAdapter;
    BluetoothSocket btSocket = null;
    String name;

    // Bluetooth streams
    InputStream  mBTInputStream  = null;
    OutputStream mBTOutputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // convert button Ids into vars
        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv = findViewById(R.id.pairedTv);
        mConnectionTv = findViewById(R.id.connectionTv);
        mBlueIv = findViewById(R.id.bluetoothIv);
        mOnBtn = findViewById(R.id.onBtn);
        mOffBtn = findViewById(R.id.offBtn);
        mDiscoveredBtn = findViewById(R.id.disc);
        mPairedBtn = findViewById(R.id.pairedBtn);
        mConnectBtn = findViewById(R.id.connectBtn);

        // UUID
        final UUID myUUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");



        // end variables ---------------------------------------------------------------------------

        //adapter
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        // check if bluetooth is available
        if (mBlueAdapter == null) {
            mStatusBlueTv.setText("Bluetooth not available");
        } else {
            mStatusBlueTv.setText("Bluetooth is available");
        }

        //set image to bluetooth status
        if (mBlueAdapter.isEnabled()) {
            mBlueIv.setImageResource(R.drawable.ic_action_on);
        } else {
            mBlueIv.setImageResource(R.drawable.ic_action_off);
        }

        // on button functionality ------------------------------------------
        mOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBlueAdapter.isEnabled()) {
                    showToast("Turning on Bluetooth...");
                    //Intent to turn on Bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else {
                    showToast("Bluetooth is already enabled");
                }

            }
        });

        //discover bluetooth btn
        mDiscoveredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBlueAdapter.isDiscovering()) {
                    showToast("Making device discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }

            }
        });

        //off btn click
        mOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    mBlueAdapter.disable();
                    showToast("Turning Bluetooth off");
                    mBlueIv.setImageResource(R.drawable.ic_action_off);
                } else {
                    showToast("Bluetooth is already off");
                }

            }
        });

        //get paired devices btn click
        mPairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    mPairedTv.setText("Paired Devices");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        mPairedTv.append("\nDevice: " + device.getName() + "," + device);
                    }

                } else {
                    // bluetooth is off so cannot get paired devices
                    showToast("Turn on bluetooth to list devices");
                }

            }
        });


        // Connect to a BT device ------------------------------------------------------------------
        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    String address = null;
                    BluetoothDevice bt = null;
                    Set<BluetoothDevice> pairedDevices = null;
                    try {

                        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
                        address = mBlueAdapter.getAddress();
                        bt = mBlueAdapter.getRemoteDevice(address);
                        pairedDevices = mBlueAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0){
                            Iterator var2 = pairedDevices.iterator();

                            while(var2.hasNext()){
                                bt = (BluetoothDevice)var2.next();
                                address = bt.getAddress();
                                name = bt.getName();
                                showToast("Connecting to: " + name);
                            }
                        }


                    } catch (Exception var4){
                    }

                    mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
                    bt = mBlueAdapter.getRemoteDevice(address);


                    try {
                        btSocket = bt.createRfcommSocketToServiceRecord(myUUID);
                        mBlueAdapter.cancelDiscovery();
                        btSocket.connect();

                    } catch (Exception e) {
                        showToast("Connection Failed." + e);
                        try{
                            showToast("Attempting fallback method...");
                            Method m = bt.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
                            btSocket = (BluetoothSocket) m.invoke(bt, 1);
                            btSocket.connect();
                        } catch (Exception e2) {
                            showToast("сука блять vodka machine broke: " + e2);
                        }
                    }
                    try {
                        mBTOutputStream = btSocket.getOutputStream();
                        mBTInputStream  = btSocket.getInputStream();
                    } catch (Exception e) {
                        showToast ("connect(): Error attaching i/o streams to socket. msg=" + e.getMessage());

                    }


                    mConnectionTv.setText("Connected to: " + name + " " + address);

                    String test = "Hello World!";
                    try {
                        mBTOutputStream.write(test.getBytes());
                    } catch (IOException e3) {
                        showToast("fail: " +e3);
                    }


                } else {
                    // bluetooth is off so cannot get paired devices
                    showToast("Turn on Bluetooth");
                }
            }
        });




    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (requestCode == RESULT_OK){
                    // bluetooth is on
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                    showToast("Bluetooth is on");
                }
                else {
                    // user denied BT turn on
                    showToast("Could not turn on bluetooth");
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //toast?!? message function
    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
