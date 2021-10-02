package com.example.cse2200_project;

import android.content.res.AssetManager;
import android.widget.Toast;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import fi.iki.elonen.NanoFileUpload;
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
        else if(uri.equals("/download/") || uri.equals("/download")){
            resp = download(session);
        }
        else if(uri.equals("/get_search_result/") || uri.equals("/get_search_result")){
            resp = get_search_result(session);
        }
        else if(uri.equals("/upload_files/") || uri.equals("/upload_files")){
            resp = upload(session);
        }
        else if(uri.equals("/create_directory/") || uri.equals("/create_directory")){
            resp = create_directory(session);
        }
        else{
            resp = error_404_view(session);
        }
        return resp;
    }

    private Response index_view(IHTTPSession session) {
        String relative_path = session.getUri().substring(3);
        String root = String.valueOf(settings.get("shared_directory"));
        String path = root + relative_path;
        File dir = new File(path);
        if(dir.exists() && dir.isFile()){
            Response resp = newFixedLengthResponse(Status.REDIRECT, "text/html", "");
            try {
                resp.addHeader("location", "/download/?path="+ URLEncoder.encode(relative_path, StandardCharsets.UTF_8.toString())+"&inline=true");
            } catch (UnsupportedEncodingException e) {
                return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \"" + e.toString() + "\"}");
            }
            return resp;
        }

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
                        else details.put("size", get_human_readable_size(file.length()));

                        Date date = new Date(file.lastModified());
                        details.put("date", date);
                        contents.put(file.getPath().substring(root.length()+1), details);
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
        }
        catch(Exception err){
            return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \""+err.toString() + "\" }");
        }
        return newFixedLengthResponse(Status.OK, "application/json", json_resp.toString());
    }


    private Response download(IHTTPSession session){
        JSONObject json_resp = new JSONObject();

        try{
            String relative_path = session.getParameters().get("path").get(0);
            Object is_inline = session.getParameters().get("inline");
            String root = String.valueOf(settings.get("shared_directory"));
            String path = root + relative_path;


            File file = new File(path);
            if(file.exists()){
                if(file.isFile()){
                    Response resp = serve_file(new FileInputStream(file), getMimeTypeForFile(relative_path));

                    if(is_inline == null) resp.addHeader("content-disposition", "attachment; filename=\""+ file.getName() +"\"");
                    else resp.addHeader("content-disposition", "inline; filename=\""+ file.getName() +"\"");

                    return resp;
                }else{
                    json_resp.put("status", "failed");
                    json_resp.put("details", "Not a file!");
                }
            }else{
                json_resp.put("status", "failed");
                json_resp.put("details", "File does not exist!");
            }
        }
        catch(Exception err){
            return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \""+err.toString() + "\" }");
        }
        return newFixedLengthResponse(Status.OK, "application/json", json_resp.toString());
    }

    String get_human_readable_size(long size_in_bytes){
        String size = "";
        if(size_in_bytes/(1000.0 * 1000.0 * 1000.0)> .9){
            size += String.format(Locale.US, "%.2f GB", size_in_bytes/(1000.0 * 1000.0 * 1000.0));
        }
        else if(size_in_bytes/(1000.0 * 1000.0) > .9){
            size += String.format(Locale.US, "%.2f MB", size_in_bytes/(1000.0 * 1000.0));
        }
        else if(size_in_bytes/(1000.0) > .9){
            size += String.format(Locale.US, "%.2f KB", size_in_bytes/(1000.0));
        }
        else{
            size += size_in_bytes + " B";
        }
        return size;
    }


    private Response get_search_result(IHTTPSession session){
        JSONObject json_resp = new JSONObject();

        try{
            String query = session.getParameters().get("query").get(0);
            query = query.toLowerCase(Locale.ROOT);
            List<String> dir_path_query = session.getParameters().get("dir_path");
            String relative_path = "";
            if(dir_path_query != null) relative_path = dir_path_query.get(0);

            String root = String.valueOf(settings.get("shared_directory"));
            String path = root + relative_path;


            File dir = new File(path);
            if(dir.exists()){
                if(dir.isDirectory()){
                    json_resp.put("status", "success");
                    json_resp.put("details", "Fetched all available contents from directory " + relative_path);

                    JSONObject data = new JSONObject();
                    data.put("current_directory", "search result for '" + query + "'");
                    data.put("parent_directory", null);



                    JSONObject contents = new JSONObject();
                    File[] files_arr = dir.listFiles();
                    if(files_arr == null){
                        return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \" could not load files \" }");
                    }
                    Queue<File> files = new LinkedList<>(Arrays.asList(files_arr));
                    while(!files.isEmpty()) {

                        File file = files.remove();
                        if(file.isDirectory()){
                            File[] sub_dir_contents = file.listFiles();
                            if(sub_dir_contents!=null)files.addAll(Arrays.asList(sub_dir_contents));
                        }
                        if (file.getName().toLowerCase(Locale.ROOT).contains(query)) {
                            JSONObject details = new JSONObject();
                            details.put("name", file.getName());
                            details.put("is_directory", file.isDirectory());
                            details.put("directory", file.getParent().substring(root.length()));
                            if (file.isDirectory()) {
                                details.put("size", "-");
                            } else details.put("size", get_human_readable_size(file.length()));

                            Date date = new Date(file.lastModified());
                            details.put("date", date);
                            contents.put(file.getPath().substring(root.length() + 1), details);
                        }
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
        }
        catch(Exception err){
            return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \""+err.toString() + "\" }");
        }
        return newFixedLengthResponse(Status.OK, "application/json", json_resp.toString());
    }

    private Response create_directory(IHTTPSession session){
        Method method = session.getMethod();
        if(!method.equals(Method.POST)){
            return newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, "application/json", "");
        }
        Response resp;
        boolean upload_permission = (boolean) settings.get("upload_permission");
        if(!upload_permission){
            return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \"Permission Denied!\"}");
        }

        List<String> dir_path_query = session.getParameters().get("dir_path");
        String relative_path = "";
        if (dir_path_query != null) relative_path = dir_path_query.get(0);

        List<String> new_dir_name_query = session.getParameters().get("new_dir_name");
        String new_dir_name = "";
        if (new_dir_name_query != null) new_dir_name = new_dir_name_query.get(0);

        String root = String.valueOf(settings.get("shared_directory"));
        String path = root + relative_path;


        File dir = new File(path);
        File new_dir = new File(path + "/" + new_dir_name);

        if(dir.exists()){
            if(dir.isDirectory()){
                if(new_dir.mkdir()){
                    resp = newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"success\", \"details\": \"New directory has been created!\"}");
                }else{
                    resp = newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \"Could not create new directory!\"}");
                }
            }else{
                resp = newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \"Not a directory!\"}");
            }
        }else{
            resp = newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \"Directory not found!\"}");
        }

        return resp;
    }

    private Response upload(IHTTPSession session){
        Method method = session.getMethod();
        if(!method.equals(Method.POST)){
            return newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, "application/json", "");
        }
        Response resp;
        boolean upload_permission = (boolean) settings.get("upload_permission");
        if(!upload_permission){
            return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \"Permission Denied!\"}");
        }
        
        List<FileItem> files;
        try{
            files = new NanoFileUpload(new DiskFileItemFactory()).parseRequest(session);
        }catch (FileUploadException err){
            return newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \"Upload failed!\"}");
        }

        List<String> dir_path_query = session.getParameters().get("dir_path");
        String relative_path = "";
        if (dir_path_query != null) relative_path = dir_path_query.get(0);

        String root = String.valueOf(settings.get("shared_directory"));
        String path = root + relative_path;


        File dir = new File(path);
        int count = 0;

        if(dir.exists()){
            if(dir.isDirectory()){
                for(int i = 0; i<files.size(); ++i){
                    FileItem fileitem = files.get(i);
                    try{
                        File file = new File(path, fileitem.getName());
                        fileitem.write(file);
                        ++count;
                    }catch (Exception err){
                        Toast.makeText(settings.context, "Could not save file '"+fileitem.getName()+"'!", Toast.LENGTH_SHORT).show();
                    }
                }
                resp = newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"success\", \"details\": \"Files uploaded. ("+ count + "/" + files.size() +")\"}");
            }else{
                resp = newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \"Not a directory!\"}");
            }
        }else{
            resp = newFixedLengthResponse(Status.OK, "application/json", "{\"status\": \"failed\", \"details\": \"Directory not found!\"}");
        }

        return resp;
    }
}
