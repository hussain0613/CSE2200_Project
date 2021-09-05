package com.example.cse2200_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button b;
    TextView tv;
    Settings settings;
    NanoServer server;
    WifiManager wifimanager;
    String ipAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = new Settings(this);
        server = new NanoServer(settings, getAssets());
        wifimanager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        ipAddress = String.valueOf(wifimanager.getConnectionInfo().getIpAddress());

        b = findViewById(R.id.button);
        tv = findViewById(R.id.textView);

        tv.setText("No Server is Running");

        b.setOnClickListener(v-> {
                if(b.getText().equals("Start")){
                    b.setText("Stop");
//                    tv.setText("Server Running");
                    /*
                    * haven to start server
                    * */
                    try {
                        wifimanager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                        ipAddress = Formatter.formatIpAddress(wifimanager.getConnectionInfo().getIpAddress());
                        settings = new Settings(this);
                        server = new NanoServer(settings, getAssets()); // an extremely inefficient way i think, should find something better
                        server.start();
                        String display_ip_addr = server.getHostname();
                        if(!display_ip_addr.equals(ipAddress)){
                            if(display_ip_addr.equals("0.0.0.0")){
                                display_ip_addr = ipAddress;
                            }
                        }
                        tv.setText("Server Running. Visit http://" + display_ip_addr+":"+server.getListeningPort() + " for transferring files");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Can't run server. Please try again, probably with different ip and port", Toast.LENGTH_LONG).show();
                    }
                }
                else if(b.getText().equals("Stop")){
                    b.setText("Start");
                    tv.setText("No Server is Running");
                    /*
                    * have to stop server
                    * */
                    server.stop();
                }
        });
    }

    public void go_to_settings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}