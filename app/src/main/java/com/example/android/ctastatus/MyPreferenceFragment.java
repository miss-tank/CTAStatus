package com.example.android.ctastatus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.format.Formatter;

/**
 * Created by MissTank on 2/7/18.
 */

public class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    protected EditTextPreference connection_url_preference;
    protected EditTextPreference connection_gateway_preference;
    protected EditTextPreference connection_port_preference;
    protected EditTextPreference connection_interval;

    protected Context context = getActivity();


    private boolean WifiConnection = false;
    private boolean MobileConnection = false;

    protected String gateway_address;

    protected DhcpInfo wifi_information;
    protected String Global_Gatway="";
    protected String ConnectionType="";


    String finalURL;
    String finalTime;
    String finalPort;
    String finalIP;





    public MyPreferenceFragment()
    {
            this.finalTime="30";
            this.finalPort="3001";
            this.finalIP="0.0.0.0";
            this.finalURL="bsm.corp.transitchicago.com";
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_screen);
        context = getActivity();

        connection_url_preference = (EditTextPreference) getPreferenceScreen().findPreference("connection_url");
        connection_gateway_preference = (EditTextPreference) getPreferenceScreen().findPreference("connection_gateway");
        connection_port_preference = (EditTextPreference) getPreferenceScreen().findPreference("connection_port");
        connection_interval =(EditTextPreference)getPreferenceScreen().findPreference("connection_seconds");

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = connMgr.getActiveNetworkInfo();

        if (networkinfo != null) {
            if (networkinfo.getType() == ConnectivityManager.TYPE_WIFI) {

                WifiConnection = true;
                System.out.println("PPPThis connection wifi");
                Global_Gatway = getGatewayAddress();
                ConnectionType="wifi";
            }

            else if (networkinfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                System.out.println("PPPThis connection mobile");
                MobileConnection = true;
                ConnectionType="mob";
            }
            else
            {
                ConnectionType="none";
            }
        }
    }


    private String getGatewayAddress()
    {
        WifiManager wifi = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi_information= wifi.getDhcpInfo();
        gateway_address = String.valueOf(wifi_information.gateway);
        String Gateway = Formatter.formatIpAddress(wifi_information.gateway);
        return  Gateway;
    }



    @Override
    public void onStart() {
        super.onStart();
        updategateway();
        updateURL();
        updatePort();
        updateTime();
        sendExtra();
    }

    @Override
    public void onResume() {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        //PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        System.out.println("The key "+ key);

        if(key.equalsIgnoreCase("connection_gateway"))
        {
                updategateway();
        }
        else if(key.equalsIgnoreCase("connection_url"))
        {
                updateURL();
        }
        else if(key.equalsIgnoreCase("connection_port"))
        {
                updatePort();
        }
        else if(key.equalsIgnoreCase("connection_seconds"))
        {
                updateTime();
        }

        sendExtra();
    }

    private void sendExtra() {
        Intent service = new Intent( getActivity(), ForegroundService.class );

        service.putExtra("ConnType", "wifi");
        service.putExtra("GatewayAddress", finalIP );
        service.putExtra("URLAddress",finalURL );
        service.putExtra("portAddress", finalPort );
        service.putExtra("timeInterval", finalTime );
        context.startService(service);
    }

    private void updateTime() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        finalTime = sharedPrefs.getString("connection_seconds","");
        System.out.println("This is teh finalTime now "+ finalTime);

        if (!finalTime.isEmpty())
        {
            connection_interval.setSummary(finalTime);
        }
    }

    private void updatePort() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        finalPort= sharedPrefs.getString("connection_port", "");
        System.out.println("This is teh connection_gateway now");
        System.out.println(finalPort);

        if (!finalPort.isEmpty()) {
            connection_port_preference.setSummary(finalPort);
        }
    }

    private void updateURL() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        finalURL= sharedPrefs.getString("connection_url", "");
        System.out.println("This is teh connection_url now");
        System.out.println(finalURL);

        if (!finalURL.isEmpty()) {
            connection_url_preference.setSummary(finalURL);
        }
    }

    private void updategateway() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        finalIP= sharedPrefs.getString("connection_gateway", "");
        System.out.println("This is teh connection_gateway now");
        System.out.println(finalIP);

        if (!finalIP.isEmpty()) {
            connection_gateway_preference.setSummary(finalIP);
        }

    }

    public boolean getCheck()
    {
        return true;
    }

    public String getURL()
    {
        updateURL();
        return finalURL;
    }

}