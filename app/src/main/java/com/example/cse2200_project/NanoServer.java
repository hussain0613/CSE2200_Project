package com.example.cse2200_project;

import android.content.res.AssetManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class NanoServer extends NanoHTTPD {
    AssetManager assets;
    Settings settings;
    public NanoServer(Settings settings, AssetManager assets){
        super(String.valueOf(settings.get("host")), (int)settings.get("port"));
        this.settings = settings;
        this.assets = assets;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Response resp;

        if (uri.equals("/") || uri.equals("/fs")) {
            resp = redirect_to_fs(session);
        }
        else if(uri.startsWith("/fs/")){
            resp = index_view(session);
        }
        else if(uri.startsWith("/statics/")){
            resp = serve_statics(session);
        }
        else if(uri.equals("/favicon.ico")){
            resp = serve_icon(session);
        }
        else if(uri.equals("/get_settings") || uri.equals("/get_settings/")){
            resp = get_settings(session);
        }
        else if(uri.equals("/get_contents") || uri.equals("/get_contents/") ){
            resp = get_contents(session);
        }
        else{
            resp = error_404_view(session);
        }
        return resp;
    }

    private Response index_view(IHTTPSession session) {
        try{
            String fn = "www/index.html";
            return serve_file(assets.open(fn), getMimeTypeForFile(fn));
        }catch(IOException err){
//            err.printStackTrace();
            return newFixedLengthResponse(Status.NOT_FOUND,"text/html","<html><body><h1>Index file not found</h1></body></html>");
        }
    }

    private Response redirect_to_fs(IHTTPSession session){
        Response resp = newFixedLengthResponse(Status.REDIRECT, "text/html", "");
        resp.addHeader("location", "/fs/");
        return resp;
    }

    private Response error_404_view(IHTTPSession session){
        try{
            String fn = "www/error.html";
            return serve_file(assets.open(fn), getMimeTypeForFile(fn));
        }catch(IOException err){
//            err.printStackTrace();
            return newFixedLengthResponse(Status.NOT_FOUND,"text/html","<html><body><h1>Error file not found</h1></body></html>");
        }
    }

    private Response serve_file(InputStream is, String mime) throws IOException {
        Response res;
        res = newFixedLengthResponse(Status.OK, mime, is, is.available());
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response serve_statics(IHTTPSession session){
        String uri = session.getUri();

        try{
            String fn = "www"+uri;
//            System.out.println("********************** static fn: " + fn);
            return serve_file(assets.open(fn), getMimeTypeForFile(fn));
        }catch(IOException err){
//            err.printStackTrace();
            return newFixedLengthResponse(Status.NOT_FOUND,"text/html","<html><body><h1>Static file not found</h1></body></html>");
        }
    }

    private  Response serve_icon(IHTTPSession session){
        try{
            String fn = "www/statics/res/favicon.ico";
//            System.out.println("********************** static fn: " + fn);
            return serve_file(assets.open(fn), getMimeTypeForFile(fn));
        }catch(IOException err){
//            err.printStackTrace();
            return newFixedLengthResponse(Status.NOT_FOUND,"text/html","<html><body><h1>Static file not found</h1></body></html>");
        }
    }

    private  Response get_settings(IHTTPSession session){
        JSONObject json = new JSONObject();
        try{
            json.put("status", "success");
            json.put("details", "fetched settings successfully");
            json.put("settings", settings.settings);
        }catch (JSONException err){
//            err.printStackTrace();
            return newFixedLengthResponse(Status.NOT_FOUND,"application/json","{'status': 'failed', 'details':'settings fetching failed'}");
        }

        Response resp = newFixedLengthResponse(json.toString());
        resp.addHeader("content-type", "application/json");
        return resp;
    }

    private Response get_contents(IHTTPSession session){
        JSONObject json_resp = new JSONObject();

        try{
            String relative_path = session.getParameters().get("dir_path").get(0);
            String root = String.valueOf(settings.get("shared_directory"));
            String path = root + relative_path;


            File dir = new File(path);
            if(dir.exists()){
                if(dir.isDirectory()){
                    json_resp.put("status", "success");
                    json_resp.put("details", "Fetched all available contents from directory " + relative_path);

                    JSONObject data = new JSONObject();
                    data.put("current_directory", relative_path);
                    if(relative_path.equals("/")) data.put("parent_directory", null);
                    else data.put("parent_directory", dir.getParent().substring(root.length()));


                    JSONObject contents = new JSONObject();
                    File[] files = dir.listFiles();
                    if(files == null){
                        return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \" could not load files \" }");
                    }
                    for(int i = 0; i<files.length; ++i){
                        JSONObject details = new JSONObject();
                        File file = files[i];
                        details.put("name", file.getName());
                        details.put("is_directory", file.isDirectory());

                        if(file.isDirectory()) details.put("size", "-");
                        else details.put("size", file.length()); // modify kore human readable size pathate hbe

                        Date date = new Date(file.lastModified());
                        details.put("date", date);
                        contents.put(file.getPath().substring(root.length()+1), details); // might need to modify later
                    }
                    data.put("contents", contents);
                    json_resp.put("data", data);

                }else{
                    json_resp.put("status", "failed");
                    json_resp.put("details", "Not a directory!");
                }
            }else{
               json_resp.put("status", "failed");
               json_resp.put("details", "Directory does not exist");
            }
        }catch (JSONException err){
            //err.printStackTrace();
            return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \""+err.toString()+ "\" }");
        }
        catch(Exception err){
            return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \""+err.toString() + "\" }");
        }
        return newFixedLengthResponse(Status.OK, "application/json", json_resp.toString());
    }
}
