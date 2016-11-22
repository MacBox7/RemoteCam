package com.remotecam;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiConfiguration;
import android.util.TypedValue;
import android.view.MenuItem;
import android.content.*;
import android.net.wifi.*;
import java.lang.reflect.*;

        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.net.wifi.WifiManager;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends Activity {

    WifiConfiguration wificonfiguration;
    Button buttonCamera , buttonScreen;
    boolean resume;
    WifiManager wifimanager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        buttonCamera = (Button)findViewById(R.id.button);
        buttonScreen = (Button)findViewById(R.id.button2);

        buttonCamera.setOnClickListener(ButtonListner);
        buttonScreen.setOnClickListener(ButtonListner);


    }
    @Override
    protected void onPause()
    {
        super.onPause();
        finish();
    }
    private View.OnClickListener ButtonListner = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button:
                    Context context = MainActivity.this;
                    wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
                    wificonfiguration = null;
                    try {
                        wifimanager.setWifiEnabled(false);
                        Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                        method.invoke(wifimanager, wificonfiguration, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent i = new Intent(MainActivity.this, CameraServer.class);
                    startActivity(i);

                    break;
                case R.id.button2:
                    context = MainActivity.this;
                    wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
                    wificonfiguration = null;
                    try {

                        Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                        method.invoke(wifimanager, wificonfiguration, false);

                        wifimanager.setWifiEnabled(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    i = new Intent(MainActivity.this, AvailableConnections.class);
                    startActivity(i);

                    break;
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public void onBackPressed()
    {


    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        System.gc();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
                int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onRestart()
    {
        super.onRestart();
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        buttonCamera = (Button)findViewById(R.id.button);
        buttonScreen = (Button)findViewById(R.id.button2);

        buttonCamera.setOnClickListener(ButtonListner);
        buttonScreen.setOnClickListener(ButtonListner);
    }

}
