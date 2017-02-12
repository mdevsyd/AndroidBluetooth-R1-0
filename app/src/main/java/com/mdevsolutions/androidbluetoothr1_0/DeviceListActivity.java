package com.mdevsolutions.androidbluetoothr1_0;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;
    private ArrayAdapter mNewDevicesArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
        Button scanBtn = (Button)findViewById(R.id.button_scan);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscovery();
                // make the current view disappear
                v.setVisibility(View.GONE);
            }
        });

        // Setup up the two array adapters one for each type of device list. (paried and discovered).
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_name);
        ListView pairedListView = (ListView)findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        ListView newDevicesListView = (ListView)findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        //set onlick listeners for both lists
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        //register to receive broadcasts when a device is found
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        //get a local instance of the BT adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        //obtain currently paired device list, if any, add them to the ArrayAdapter
        Set <BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if(pairedDevices.size()>0){
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices){
                mPairedDevicesArrayAdapter.add(device.getName()+ "\n" +device.getAddress());
            }
        }else{
            String noDev = getResources().getText(R.string.no_paired_devices).toString();
            mPairedDevicesArrayAdapter.add(noDev);
        }
    }

    private void startDiscovery() {
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        //TODO Progress bar of some sort
        //cancel any current discoveries if any exist
        if (mBtAdapter.isDiscovering()){
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();
    }

    /**
     * On click listener for the paired and new devices in lists
      */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //need to cancel the discovery as it is resource heavy
            mBtAdapter.cancelDiscovery();
            //TODO finidh!!!
            // MAC hardware address of the device is the last 17 chars of the view
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Log.d(Constants.DEBUG_TAG, "item was clicked " + address);
            //create intent including the hardware address
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_DEVICE_ADDRESS, address);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get the action to check if device was found
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //check if the device has already paired
                if (device.getBondState() != BluetoothDevice.BOND_BONDED){
                    mNewDevicesArrayAdapter.add(device.getName()+ "\n" +device.getAddress());
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                if (mNewDevicesArrayAdapter.getCount()==0){
                    //change display to show no devices were found
                    String noDev = getResources().getText(R.string.no_device).toString();
                    mNewDevicesArrayAdapter.add(noDev);
                }

            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // always cancel the discovery
        if (mBtAdapter !=null) {
            mBtAdapter.cancelDiscovery();
        }
        //unregister listeners to the broadcasts
        this.unregisterReceiver(mReceiver);
    }
}