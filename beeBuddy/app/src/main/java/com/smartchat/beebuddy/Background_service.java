package com.smartchat.beebuddy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.kosalgeek.asynctask.AsyncResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

public class Background_service extends Service implements AsyncResponse
{
    String phoneno = null;
    String name = null;
    boolean myflag = false;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void Service_Notification() {
        CharSequence text = "You have new message !!!";

        // The PendingIntent to launch our activity if the user selects this notification




        Intent intent = new Intent(this,Splash.class);

        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);

        PendingIntent contentIntent = PendingIntent.getActivity(this,uniqueInt,intent, 0);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                                        .setSmallIcon(R.drawable.icon)  // the status icon
                                        .setTicker(text)  // the status text
                                        .setWhen(System.currentTimeMillis())  // the time stamp
                                        .setSound(alarmSound)
                                        .setContentTitle("New Message")  // the label of the entry
                                        .setContentText(text)  // the contents of the entry
                                        .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                                        .build();
        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNM.notify(0, notification);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //Service_Notification();
        //phoneno = intent.getStringExtra("id");
        myflag = true;
        try {
            SharedPreferences sp = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
            phoneno = sp.getString("phoneno", null);
            name = sp.getString("name", null);
        } catch(Exception e) {
            ;
        }
        final Thread thread = new Thread() {
            public void run() {
                while (myflag) {
                    try {
                        mycall();
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myflag = false;
        //Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show();
    }
    public String mycall()
    {
        try {
            Properties sysProperties = System.getProperties();
            sysProperties.put("proxyHost", "http://proxy.iiit.ac.in");
            sysProperties.put("proxyPort", "8080");
            sysProperties.put("proxySet", "true");
            String myurl="http://10.2.130.233/user_control.php";
            URL url = new URL(myurl);
            HttpURLConnection httpurl_conn = (HttpURLConnection) url.openConnection();
            httpurl_conn.setRequestMethod("POST");
            httpurl_conn.setDoOutput(true);
            httpurl_conn.setDoInput(true);
            OutputStream os = httpurl_conn.getOutputStream();
            BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(os,"UTF-8"));
            String post_data = URLEncoder.encode("phoneno", "UTF-8") + "="
                                + URLEncoder.encode(phoneno,"UTF-8") + "&"
                                + URLEncoder.encode("name", "UTF-8") + "="
                                + URLEncoder.encode(name,"UTF-8") + "&"
                                + URLEncoder.encode("check","UTF-8") + "="
                                + URLEncoder.encode("1","UTF-8");
            bw.write(post_data);
            bw.flush();
            bw.close();
            os.close();
            InputStream inputstream = httpurl_conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream,"iso-8859-1"));
            String result="",line="";
            while((line = br.readLine()) != null) {
                result += line;
            }
            br.close();
            inputstream.close();
            httpurl_conn.disconnect();
            // we are just checking if there is any message for current user
            if(result != null) {
                if(result.equals("yes")) {
                    Service_Notification();
                }
            }
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void processFinish(String s)
    {
        //Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
