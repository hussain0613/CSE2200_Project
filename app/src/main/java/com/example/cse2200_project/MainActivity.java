package com.example.cse2200_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button b;
    TextView tv;
    Settings settings;
    NanoServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = new Settings(this);
        server = new NanoServer(settings, getAssets());

        b = findViewById(R.id.button);
        tv = findViewById(R.id.textView);

        tv.setText("No Server is Running");

        b.setOnClickListener(v-> {
                if(b.getText().equals("Start")){
                    b.setText("Stop");
                    tv.setText("Server Running");
                    /*
                    * haven to start server
                    * */
                    try {
                        settings = new Settings(this);
                        server = new NanoServer(settings, getAssets()); // an extremely inefficient way i think, should find something better
                        server.start();
                    } catch (IOException e) {
                        e.printStackTrace();
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