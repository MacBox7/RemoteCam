package com.remotecam;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Formatter;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class AvailableConnections extends Activity  {
    ListView lv;
    WifiManager wifi;
    String wifis[];
    WifiScanReceiver wifiReciever;
    List<ScanResult> wifiScanList;
    int id1;
    private String ipAddressString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_connections);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        lv=(ListView)findViewById(R.id.listView);
        wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        wifi.setWifiEnabled(true);
        wifi.startScan();
        listCallback();
    }

    public void listCallback()
    {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiConfiguration config = new WifiConfiguration();
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.SSID = "\"" + wifiScanList.get(position).SSID + "\"";
                id1 = wifi.addNetwork(config);
                wifi.enableNetwork(id1, true);

                Intent intent = new Intent(AvailableConnections.this, liveFeed.class);
                startActivity(intent);
            }
        });
    }
    public void onBackPressed(){
        Intent intent = new Intent(AvailableConnections.this,MainActivity.class);
        startActivity(intent);
        return;
    }
    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    private class WifiScanReceiver extends BroadcastReceiver{
        public void onReceive(Context c, Intent intent) {
            wifiScanList = wifi.getScanResults();
            wifis = new String[wifiScanList.size()];
            //wifi_ssid = new int[wifiScanList.size()];

            for (int i = 0; i < wifiScanList.size(); i++){
                wifis[i] = ((wifiScanList.get(i).SSID).toString());
            }
            lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,wifis));
        }
    }
}