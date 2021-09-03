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
    Button save_btn, reset_default_btn;

    Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new Settings(this);

        shared_dir_et = findViewById(R.id.shared_directory_field);
        host_et = findViewById(R.id.host_field);
        port_et = findViewById(R.id.port_field);

        sub_dir_tb = findViewById(R.id.share_subdir_field);
        upload_tb = findViewById(R.id.upload_permission_field);
        mod_tb = findViewById(R.id.modify_permission_field);

        set_from_file();

        save_btn = findViewById(R.id.save_btn);
        reset_default_btn = findViewById(R.id.reset_default_btn);

        findViewById(R.id.back_img).setOnClickListener(e->go_back());
        findViewById(R.id.edit_img).setOnClickListener(e->toggle_edit());

        save_btn.setOnClickListener(e->save());
        reset_default_btn.setOnClickListener(e -> reset_default());
    }

    void go_back(){
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    void toggle_edit(){
        set_enable_all(!shared_dir_et.isEnabled());
    }

    void save(){
        set_to_file();
        set_enable_all(false);
    }

    void reset_default(){
        settings.setDefault();
        set_from_file();
    }

    void set_enable_all(boolean flag){
        shared_dir_et.setEnabled(flag);
        host_et.setEnabled(flag);
        port_et.setEnabled(flag);

        sub_dir_tb.setEnabled(flag);
        upload_tb.setEnabled(flag);
        mod_tb.setEnabled(flag);

        if(flag){
            save_btn.setVisibility(View.VISIBLE);
            reset_default_btn.setVisibility(View.VISIBLE);
        }
        else {
            save_btn.setVisibility(View.GONE);
            reset_default_btn.setVisibility(View.GONE);
        }
    }

    void set_from_file(){
        shared_dir_et.setText((String)settings.get("shared_directory"));
        host_et.setText((String) settings.get("host"));
        port_et.setText(String.valueOf(settings.get("port")));

        sub_dir_tb.setChecked((boolean) settings.get("share_sub_directory"));
        upload_tb.setChecked((boolean) settings.get("upload_permission"));
        mod_tb.setChecked((boolean) settings.get("modify_permission"));
    }
    void set_to_file(){
        settings.set("shared_directory", shared_dir_et.getText().toString());
        settings.set("host", host_et.getText().toString());
        settings.set("port", Integer.parseInt(port_et.getText().toString()));

        settings.set("share_sub_directory", sub_dir_tb.isChecked());
        settings.set("upload_permission", upload_tb.isChecked());
        settings.set("modify_permission", mod_tb.isChecked());
        settings.write_settings();
    }
}