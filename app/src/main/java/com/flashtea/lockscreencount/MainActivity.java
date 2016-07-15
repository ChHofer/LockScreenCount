package com.flashtea.lockscreencount;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
    SharedPreferences sharedPreferences;
    CheckBox vibrateCheckBox, shakeCheckBox;

    BroadcastReceiver broadcastReceiverAdd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sharedPreferences = getApplicationContext().getSharedPreferences(
                    getString(R.string.preferencekey), Context.MODE_PRIVATE);
            count = sharedPreferences.getInt("count",0);
            count++;
            sharedPreferences.edit().putInt("count",count).apply();
            updateNotification();
        }
    };

    BroadcastReceiver broadcastReceiverReset = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sharedPreferences = getApplicationContext().getSharedPreferences(
                    getString(R.string.preferencekey), Context.MODE_PRIVATE);
            sharedPreferences.edit().putInt("count",0).apply();
            updateNotification();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shakeCheckBox = (CheckBox) findViewById(R.id.shakeCheckBox);
        sharedPreferences = getApplicationContext().getSharedPreferences(
                getString(R.string.preferencekey), Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean("shake",false)){
            shakeCheckBox.setChecked(true);
        }else{
            shakeCheckBox.setChecked(false);
        }

        vibrateCheckBox = (CheckBox) findViewById(R.id.shakeCheckBox);
        sharedPreferences = getApplicationContext().getSharedPreferences(
                getString(R.string.preferencekey), Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean("vibrate",false)){
            vibrateCheckBox.setChecked(true);
        }else{
            vibrateCheckBox.setChecked(false);
        }

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
                if(sharedPreferences.getBoolean("shake",false)){
                    if(notificationManager!= null) {
                        if (notificationManager.getActiveNotifications().length > 0) {
                            Log.w("flashtea.log", "Shake");
                            sharedPreferences = getApplicationContext().getSharedPreferences(
                                    getString(R.string.preferencekey), Context.MODE_PRIVATE);
                            count = sharedPreferences.getInt("count",0);
                            count++;
                            sharedPreferences.edit().putInt("count",count).apply();
                            updateNotification();
                        }
                    }
                }
            }
        });
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.shakeCheckBox:
                if (checked) {
                    sharedPreferences.edit().putBoolean("shake",true).apply();
                }else{
                    sharedPreferences.edit().putBoolean("shake",false).apply();
                }
                break;
            case R.id.vibrateCheckBox:
                if (checked) {
                    sharedPreferences.edit().putBoolean("vibrate",true).apply();
                }else{
                    sharedPreferences.edit().putBoolean("vibrate",false).apply();
                }
                break;
        }
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
        updateNotification();
    }

    public void removeNotification(){
        String ns = Context.NOTIFICATION_SERVICE;
        notificationManager = (NotificationManager) getSystemService(ns);

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
        sharedPreferences = getApplicationContext().getSharedPreferences(
                getString(R.string.preferencekey), Context.MODE_PRIVATE);
        count = sharedPreferences.getInt("count",0);

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

        notificationView.setTextViewText(R.id.appName,String.valueOf(String.valueOf(count)));

        notificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());

        if(sharedPreferences.getBoolean("vibrate",false)){
            v.vibrate(100);
        }
    }


}
