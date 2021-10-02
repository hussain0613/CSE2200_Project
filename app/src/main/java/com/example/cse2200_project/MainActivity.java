package com.example.cse2200_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.Manifest;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView textView;

    NanoServer server;
    Settings settings;

    WifiManager wifimanager;
    String ipAddress;

    Intent notification_intent;

    static String SERVER_URL;
    String buttonText = "Start";
    String textViewText = "No Server Running";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        wifimanager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);


        notification_intent = new Intent(this, ForegroundService.class);

        if(Build.VERSION.SDK_INT >= 23) {
            String [] needed_permission = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
            requestPermissions(needed_permission, 138);
        }
        if(Build.VERSION.SDK_INT >= 30){
            String [] needed_permission = {
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
            };
            requestPermissions(needed_permission, 138);
        }

        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);

        textView.setText(textViewText);

        button.setOnClickListener(v-> {
                if(buttonText.equals("Start")){
                    ipAddress = Formatter.formatIpAddress(wifimanager.getConnectionInfo().getIpAddress());
                    settings = new Settings(getApplicationContext());
                    server = new NanoServer(settings, getAssets());

                    String display_ip_address = server.getHostname();
                    if(!display_ip_address.equals(ipAddress)){
                        if(display_ip_address.equals("0.0.0.0")){
                            display_ip_address = ipAddress;
                        }
                    }
                    SERVER_URL = "http://" + display_ip_address+":"+ settings.get("port");

                    try {
                        server.start();
                        startService(notification_intent);
                        buttonText = "Stop";
                        textViewText = "Server Running.\nVisit " + SERVER_URL + " for transferring files";
                    } catch (IOException e) {
                        //e.printStackTrace();
                        Toast.makeText(this, "Can't start server. Please try again letter, probably with different IP and/or Port in Settings", Toast.LENGTH_LONG).show();
                    }
                }
                else if(buttonText.equals("Stop")){
                    server.stop();
                    stopService(notification_intent);
                    buttonText = "Start";
                    textViewText = "No Server Running";
                }
            button.setText(buttonText);
            textView.setText(textViewText);
        });
    }


    public void go_to_settings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(notification_intent);
        if(server!= null) server.stop();
    }
}