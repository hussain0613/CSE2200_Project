package com.example.cse2200_project;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

public class Settings implements Serializable{
    Context context;
    JSONObject settings;

    int server_state;


    public Settings(Context context) {
        this.context = context;
        settings = new JSONObject();
        server_state = 0;
        readSettings();
    }

    void readSettings(){
        try {
            //Toast.makeText(context, "Loading Settings", Toast.LENGTH_SHORT).show();
            File file = new File(context.getFilesDir(), "settings.json");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(new FileReader(file));

            StringBuilder sb = new StringBuilder();
            int ch;

            while((ch = br.read())!=-1){
                sb.append((char)ch);
            }
            settings = new JSONObject(sb.toString());
            br.close();
            fr.close();
            //Toast.makeText(context, "Loaded Settings Successfully", Toast.LENGTH_SHORT).show();

        }catch (FileNotFoundException err ){
            Toast.makeText(context, "Settings file not found, will try to create settings file", Toast.LENGTH_SHORT).show();
            setDefault();
            write_settings();
            Toast.makeText(context, "Settings set to default", Toast.LENGTH_SHORT).show();
        }catch (IOException err){
            Toast.makeText(context, "Can't Read Settings file, will try to create settings file", Toast.LENGTH_SHORT).show();
            setDefault();
            write_settings();
            Toast.makeText(context, "Settings set to default", Toast.LENGTH_SHORT).show();
        }catch (JSONException err){
            Toast.makeText(context, "Can't convert to Settings JSON", Toast.LENGTH_SHORT).show();
            err.printStackTrace();
        }catch (Exception err){
            Toast.makeText(context, "Error reading settings" + err.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void write_settings(){
        Toast.makeText(context, "Saving Settings", Toast.LENGTH_SHORT).show();
        try {
            File file = new File(context.getFilesDir(), "settings.json");
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);

            String settings_str = settings.toString();

            for(int i = 0; i<settings_str.length(); ++i){
                bw.write(settings_str.charAt(i));
            }
            bw.close();
            fw.close();
            Toast.makeText(context, "Settings saved.", Toast.LENGTH_SHORT).show();
        }catch (IOException err){
            err.printStackTrace();
            Toast.makeText(context, "Can't Write to Settings File", Toast.LENGTH_SHORT).show();
        }
    }


    public Object get(String settings_name){
        try{
            return settings.get(settings_name);
        }catch (JSONException err){
            err.printStackTrace();
            Toast.makeText(context, "Can't Get Value from Settings JSON", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public void set(String settings_name, Object settings_value){
        try {
            settings.put(settings_name, settings_value);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Can't Write Value to Settings JSON", Toast.LENGTH_SHORT).show();
        }
    }

    void setDefault(){
        if(settings == null) settings = new JSONObject();

        String path = Environment.getExternalStorageDirectory().getPath();
        Toast.makeText(context, "path: "+ path, Toast.LENGTH_SHORT).show();
        set("shared_directory", path);
        set("share_sub_directory", true);
        set("upload_permission", true);
        set("modify_permission", false);
        set("host", "0.0.0.0");
        set("port", 9999);
    }

}
