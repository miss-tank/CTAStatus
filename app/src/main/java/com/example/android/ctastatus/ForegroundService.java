package com.example.android.ctastatus;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by MissTank on 1/26/18.
 */

public class ForegroundService extends Service {
    protected static boolean IS_SERVICE_RUNNING = false;
    protected static String CONNECTION_URL = "www.bsm.corp.transitchicago.com/";
    protected final String USER_AGENT = "Chrome/53.0.2785.143";
    protected int HttpResponseCode = 0;
    protected String connectionType = "";
    protected String GatewayAddress = "";
    protected int responseCode = 0;
    protected int wifiURL_responseCode = 0;
    protected int wifiGateway_responseCode = 0;
    protected int mobile_responsecode = 0;
    protected boolean wifiURLConnected = false;
    protected boolean wifiGatewayConnected = false;
    protected String port_Address;
    protected boolean ConnMob=false;
    protected boolean ConnWifi=false;
    protected int timeScale=0;
    boolean first=false;


    @Override
    public void onCreate()
    {
        super.onCreate();
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
    }

    /*
        Get the Extras sent from the user preference settings
        establish a connection based on the connectivity of the device.
     */
    @Override
    public int onStartCommand(Intent intent, int flogs,int startID)
    {
        MyPreferenceFragment x = new MyPreferenceFragment();
        Boolean newStart= x.getCheck();

        String user_entered_URL=intent.getStringExtra("URLAddress");
        System.out.println("user_entered_URL is "+ user_entered_URL);

        String user_entered_port="";
        String gateway ="";
        String user_interval="";

        /*
            Repeats the tasks based on the interval set by the user
            By default value is "30 seconds"
         */
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                if (ConnWifi == true)
                {
                    new connectURLToWifi().execute();
                    new connectGatewayToWifi().execute();
                }
                else if (ConnMob == true)
                {
                    new connectURLToMob().execute();
                }
            }
        };




            if( (newStart && (user_entered_URL==null) )|| (user_entered_URL.equalsIgnoreCase("")))
        {
            first = true;
            System.out.println("******* is null ");
            user_entered_URL = "bsm.corp.transitchicago.com";
            user_entered_port = "3001";
            gateway = "192.168.2.1";
            user_interval = "30";
            connectionType = "wifi";
        }
        else
        {
            System.out.println("******* not null");
            user_entered_URL = intent.getStringExtra("URLAddress");
            user_entered_port = intent.getStringExtra("portAddress");
            gateway = intent.getStringExtra("GatewayAddress");
            user_interval = intent.getStringExtra("timeInterval");
            connectionType = intent.getStringExtra("ConnType");
        }

        if(user_interval.equalsIgnoreCase("") )
        {
            user_interval="30";
        }

        timeScale=0;
        timeScale = Integer.parseInt(user_interval);
        CONNECTION_URL = "http://"+user_entered_URL;
        GatewayAddress = "http://"+ gateway +":"+user_entered_port;
        port_Address= user_entered_port;


        if(connectionType.equals("wifi"))
        {
            ConnWifi=true;
            ConnMob=false;

            try {
                    new connectURLToWifi().execute();
                    new connectGatewayToWifi().execute();
                } catch (Exception e) {
                    Log.i("this exception", String.valueOf(HttpResponseCode));
                    e.printStackTrace();
                }

        }
        else if(connectionType.equals("mob"))
        {
            ConnMob=true;
            ConnWifi=false;
            try {
                new connectURLToMob().execute();
            } catch (Exception e) {
                Log.i("this exception", String.valueOf(HttpResponseCode));
                e.printStackTrace();
            }
        }
        else
        {
            try {
            } catch (Exception e) {
                Log.i("this exception", String.valueOf(HttpResponseCode));
                e.printStackTrace();
            }
        }

        if(timeScale!=0)
        {

            System.out.println("NOT NULL INTERVAL" + timeScale);
            new Timer().scheduleAtFixedRate( task,0,timeScale*1000);
        }
        else
        {
            System.out.println("NULL INTERVAL");
        }

        return START_STICKY;
    }


    /*
        Check the response code from the WIFI connection
        Response code 200 is Green Connection
                      500 is Yellow Connection
                      for anything else, recheck using Gateway address
     */

    private class connectURLToWifi extends AsyncTask<URL, Integer, Integer> {
        protected Integer doInBackground(URL... urls) {
            wifiURLConnected=false;
            wifiGatewayConnected=false;

            URL obj  = null;
            HttpURLConnection conn = null;

            try {
                obj = new URL(CONNECTION_URL);
                Log.i("URL-WIFI ", CONNECTION_URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                conn = (HttpURLConnection)obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                conn.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }

            conn.setRequestProperty("Usere-Agent ", USER_AGENT);

            try {
                responseCode = conn.getResponseCode();
                wifiURL_responseCode= responseCode;

                Log.i("WIFI-URL response", String.valueOf(wifiURL_responseCode));

                if(wifiURL_responseCode==200 || wifiURL_responseCode==500) {
                    wifiURLConnected = true;
                    showNotificationWifi();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return wifiURL_responseCode;
        }

        protected void onProgressUpdate(Integer... progress) {}

        protected void onPostExecute(Long result) {}

    }

    /*
         Check the URL response code using the Gateway address of the device.
     */
    private class connectGatewayToWifi extends AsyncTask<URL, Integer, Integer> {
        protected Integer doInBackground(URL... urls) {

            Log.i("wifiURLConnected", String.valueOf(wifiURLConnected));

            if(!wifiURLConnected) {
                wifiGateway_responseCode=0;
                URL obj = null;
                HttpURLConnection conn = null;

                try {
                    Log.i("Gate way address ", GatewayAddress);
                    obj = new URL(GatewayAddress);


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                try {
                    conn = (HttpURLConnection) obj.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    conn.setRequestMethod("GET");
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }

                conn.setRequestProperty("Usere-Agent ", USER_AGENT);

                try {
                    wifiGateway_responseCode = conn.getResponseCode();
                    Log.i("Gateway Response ", String.valueOf(wifiGateway_responseCode));
                    if (responseCode == 200) {
                        wifiGatewayConnected = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            showNotificationWifi();
            return wifiGateway_responseCode;
        }

        protected void onProgressUpdate(Integer... progress) {}
        protected void onPostExecute(Long result) {}
    }


    /*
        Check the response code for the devices connected to mobile network.
     */
    private class connectURLToMob extends AsyncTask<URL, Integer, Integer> {
        protected Integer doInBackground(URL... urls) {

            URL obj  = null;
            HttpURLConnection conn = null;

            Log.i("Checking Mob ", "Connected");
            Log.i("MOB URL ", CONNECTION_URL);

            try {
                obj = new URL(CONNECTION_URL);
                Log.i("URL for Connection ", CONNECTION_URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                conn = (HttpURLConnection)obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                conn.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }

            conn.setRequestProperty("Usere-Agent ", USER_AGENT);


            try {
                responseCode = conn.getResponseCode();
                mobile_responsecode = responseCode;
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.i("MOB Response code ", String.valueOf(responseCode));

            showNotificationMob(mobile_responsecode);

            return responseCode;
        }

        protected void onProgressUpdate(Integer... progress) {}
        protected void onPostExecute(Long result) {}

    }




    /*
        show notification for WIFI connection based on the response code retrieved from URL.
        WIFI URL 200 Green tick
                 500 yellow exclamation
                 404
                    Gateway Response


     */


    private void showNotificationWifi()
    {
        DateFormat df = new SimpleDateFormat("h:mm:ss a");
        String date = df.format(Calendar.getInstance().getTime());
        Log.i("date", String.valueOf(date));

        Log.i(" WIFI-GW boolean ", String.valueOf(wifiGatewayConnected));
        Log.i(" WIFI-GW response ", String.valueOf(wifiGateway_responseCode));

        Log.i(" WIFI-URL boolean ", String.valueOf(wifiURLConnected));
        Log.i(" WIFI-URL response ", String.valueOf(wifiURL_responseCode));


        if(wifiURLConnected && wifiURL_responseCode==200)
        {
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(CONNECTION_URL));
            PendingIntent pendingintent = PendingIntent.getActivity(this,0,notificationIntent,0);

            Log.i("WIFI response200", "Green ALL");

            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.mipmap.tick_green);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("WIFI " +CONNECTION_URL)
                    .setContentText("Last Connection Check : " + date)
                    .setSmallIcon(R.mipmap.tick_green)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon,128,128,false))
                    .setContentIntent(pendingintent)
                    .setOngoing(true)
                    .build();
            startForeground(101,notification);

        }
        else if(wifiURLConnected && wifiURL_responseCode==500)
        {
            Log.i("WIFI response500", "CONNECTED ALL");

            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(CONNECTION_URL));
            PendingIntent pendingintent = PendingIntent.getActivity(this,0,notificationIntent,0);


            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.mipmap.yello);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Connected secure over WIFI" )
                    .setContentText(CONNECTION_URL + "\nLast Connection Check : " + date)
                    .setSmallIcon(R.mipmap.yello)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon,128,128,false))
                    .setContentIntent(pendingintent)
                    .setOngoing(true)
                    .build();
            startForeground(101,notification);
        }
        else if(wifiGatewayConnected)
        {
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(GatewayAddress));
            PendingIntent pendingintent = PendingIntent.getActivity(this,0,notificationIntent,0);

            Log.i("GatewayResponse", "gateway");

            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.mipmap.yello);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Connected secured over WIFI" )
                    .setContentText(CONNECTION_URL + "\nLast Connection Check : " + date)
                    .setSmallIcon(R.mipmap.yello)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon,128,128,false))
                    .setContentIntent(pendingintent)
                    .setOngoing(true)
                    .build();
            startForeground(101,notification);
        }
        else
        {
            Log.i("NO response", "NOCONNECTED ALL");
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(CONNECTION_URL));
            PendingIntent pendingintent = PendingIntent.getActivity(this,0,notificationIntent,0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.mipmap.off);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("CTA Connection  FAILED" )
                    .setContentText(CONNECTION_URL + "\nLast Connection Check : " + date)
                    .setSmallIcon(R.mipmap.off)
                    .setContentIntent(pendingintent)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon,128,128,false))
                    .setOngoing(true)
                    .build();
            startForeground(101,notification);
        }
    }

    private void showNotificationMob(int response)
    {
        response=0000;

        DateFormat df = new SimpleDateFormat("h:mm:ss a");
        String date = df.format(Calendar.getInstance().getTime());
        Log.i("date", String.valueOf(date));

        if(response==200)
        {
            Log.i("Using responseMOB", "200");


            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(CONNECTION_URL));
            PendingIntent pendingintent = PendingIntent.getActivity(this,0,notificationIntent,0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.mipmap.tick_green);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("CTA Connection Status: " + connectionType )
                    .setContentText(CONNECTION_URL + "\nLast Connection Check : " + date)
                    .setSmallIcon(R.mipmap.tick_green)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon,128,128,false))
                    .setContentIntent(pendingintent)
                    .setOngoing(true)
                    .build();
            startForeground(101,notification);
        }
        else
        {
            Log.i("Using responseMOB", "failed response");
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(CONNECTION_URL));
            PendingIntent pendingintent = PendingIntent.getActivity(this,0,notificationIntent,0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.mipmap.off);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("CTA Connection Failed" )
                    .setContentText(CONNECTION_URL + "\nLast Connection Check : " + date)
                    .setSmallIcon(R.mipmap.off)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon,128,128,false))
                    .setContentIntent(pendingintent)
                    .setOngoing(true)
                    .build();
            startForeground(101,notification);
        }
    }

    public void onDestroy()
    {
        super.onDestroy();

    }

    public IBinder onBind(Intent intent)
    {
        return null;
    }
}


