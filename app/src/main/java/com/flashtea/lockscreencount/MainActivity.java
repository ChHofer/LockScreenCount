package com.flashtea.lockscreencount;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

public class MainActivity extends AppCompatActivity{

    private final int NOTIFICATION_ID = 999;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder mNotifyBuilder;
    RemoteViews notificationView;
    int count = 0;
    Button showNotificationButton;
    Button removeNotificationButton;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    Vibrator v;

    BroadcastReceiver broadcastReceiverAdd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            count++;
            updateNotification();
        }
    };

    BroadcastReceiver broadcastReceiverReset = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            count=0;
            updateNotification();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(broadcastReceiverAdd, new IntentFilter("ADD_COUNT"));
        registerReceiver(broadcastReceiverReset, new IntentFilter("RESET_COUNT"));

        showNotificationButton = (Button) findViewById(R.id.show_notification_button);
        showNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNotification();
            }
        });

        removeNotificationButton = (Button) findViewById(R.id.remove_notification_button);
        removeNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeNotification();
            }
        });

        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        // Shake init
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake() {
                if(notificationManager!= null) {
                    if (notificationManager.getActiveNotifications().length > 0) {
                        Log.w("flashtea.log", "Shake");
                        count++;
                        updateNotification();

                        v.vibrate(300);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverAdd);
        unregisterReceiver(broadcastReceiverReset);
        mSensorManager.unregisterListener(mShakeDetector);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    private void startNotification(){
        String ns = Context.NOTIFICATION_SERVICE;
        notificationManager = (NotificationManager) getSystemService(ns);


        notificationView = new RemoteViews(getPackageName(),
                R.layout.activity_custom_notification);

        //the intent that is started when the notification is clicked (works)
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContent(notificationView)
                .setContentIntent(pendingNotificationIntent)
                .setContentTitle("New Message")
                .setContentText("You've received new messages.")
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher);


        //Intent that is started when Add Button is clicked.
        Intent addIntent = new Intent(this, addButtonListener.class);
        PendingIntent pendingAddIntent = PendingIntent.getBroadcast(this, 0,
                addIntent, 0);

        notificationView.setOnClickPendingIntent(R.id.add,
                pendingAddIntent);


        //Intent that is started when Reset Button is clicked.
        Intent resetIntent = new Intent(this, resetButtonListener.class);
        PendingIntent pendingResetIntent = PendingIntent.getBroadcast(this, 0,
                resetIntent, 0);

        notificationView.setOnClickPendingIntent(R.id.reset,
                pendingResetIntent);

        notificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }

    public void removeNotification(){
        notificationManager.cancelAll();
        //notificationManager.cancel(NOTIFICATION_ID);
    }

    public static class addButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.sendBroadcast(new Intent("ADD_COUNT"));
        }
    }

    public static class resetButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.sendBroadcast(new Intent("RESET_COUNT"));
        }
    }

    public void updateNotification(){
        notificationView.setTextViewText(R.id.appName,String.valueOf(count));
        mNotifyBuilder.setContent(notificationView);

        // Because the ID remains unchanged, the existing notification is
        // updated.
        notificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }


}
