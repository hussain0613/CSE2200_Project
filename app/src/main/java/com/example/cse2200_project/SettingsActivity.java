package com.example.cse2200_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {
    EditText shared_dir_et, host_et, port_et;
    ToggleButton sub_dir_tb, upload_tb, mod_tb;
    Button save_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        shared_dir_et = findViewById(R.id.shared_directory_field);
        host_et = findViewById(R.id.host_field);
        port_et = findViewById(R.id.port_field);

        sub_dir_tb = findViewById(R.id.share_subdir_field);
        upload_tb = findViewById(R.id.upload_permission_field);
        mod_tb = findViewById(R.id.modify_permission_field);

        save_btn = findViewById(R.id.save_btn);

        findViewById(R.id.back_img).setOnClickListener(e->go_back());
        findViewById(R.id.edit_img).setOnClickListener(e->toggle_edit());

        save_btn.setOnClickListener(e->save());
    }

    void go_back(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    void toggle_edit(){
        set_enable_all(!shared_dir_et.isEnabled());
    }
    void save(){

    }
    void set_enable_all(boolean flag){
        shared_dir_et.setEnabled(flag);
        host_et.setEnabled(flag);
        port_et.setEnabled(flag);

        sub_dir_tb.setEnabled(flag);
        upload_tb.setEnabled(flag);
        mod_tb.setEnabled(flag);

        if(flag)save_btn.setVisibility(View.VISIBLE);
        else save_btn.setVisibility(View.GONE);
    }
}