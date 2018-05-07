package com.example.android.ctastatus;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainActivity extends PreferenceActivity {


    protected DhcpInfo wifi_information;
    protected String Global_Gatway = "";
    protected String gateway_address = "";
    protected boolean WifiConnection = false;
    protected boolean MobileConnection = false;
    protected static boolean IS_SERVICE_RUNNING = false;
    protected static String CONNECTION_URL = "bsm.corp.transitchicago.com/";
    protected String ConnectionType = "";

    private Context context;

    /*
        This function check if the device is connected to WIFI or MOBILE network.

        WIFI:
            If the device is connected to WIFI, get the gateway address.
        MOBILE:
            If the device is connected to MOBILE NETWORK, check the response code.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        Connect();
    }

    //Connect to the service
    private void Connect() {

        final Intent service = new Intent(MainActivity.this, ForegroundService.class);

        if (!ForegroundService.IS_SERVICE_RUNNING) {
            ForegroundService.IS_SERVICE_RUNNING = true;
        } else {
            ForegroundService.IS_SERVICE_RUNNING = false;
        }

        startService(service);
    }
}