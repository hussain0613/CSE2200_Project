package com.example.cse2200_project;

import android.content.res.AssetManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
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
        else if(uri.equals("/favicon.png")){
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
            err.printStackTrace();
            return newFixedLengthResponse(Status.NOT_FOUND,"text/html","<html><body><h1>Static file not found</h1></body></html>");
        }
    }

    private  Response serve_icon(IHTTPSession session){
        try{
            String fn = "www/statics/res/favicon.ico";
//            System.out.println("********************** static fn: " + fn);
            return serve_file(assets.open(fn), getMimeTypeForFile(fn));
        }catch(IOException err){
            err.printStackTrace();
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
        try{
            String fn = "dummy_data.json";
//            System.out.println("********************** static fn: " + fn);
            Response resp = serve_file(assets.open(fn), getMimeTypeForFile(fn));
            resp.addHeader("content-type", "application/json");
//            System.out.println("************************ dummy_data.json found");
            return resp;
        }catch(IOException err){
//            err.printStackTrace();
            return newFixedLengthResponse(Status.NOT_FOUND,"application/json","{'status': 'failed', 'details':'dummy_data file not found'}");
        }
    }
}
