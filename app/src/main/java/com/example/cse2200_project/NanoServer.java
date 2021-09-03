package com.example.cse2200_project;

import java.util.Map;
import java.util.List;
import fi.iki.elonen.NanoHTTPD;

public class NanoServer extends NanoHTTPD {
    public NanoServer(){
        super("0.0.0.0", 9921);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Response resp;

        switch (uri) {
            case "/":
            case "/index":
            case "/index/":
            case "/home":
            case "/home/":
                resp = index_view(session);
                break;
            case "/about":
            case "/about/":
                resp = about_view(session);
                break;
            default:
                resp = error_404_view(session);
        }
        return resp;
    }

    public Response index_view(IHTTPSession session){
        String msg = "<html><body><h1>Index of the Demo Server running on an Android</h1>\n";
        Map<String, List<String>> parms = session.getParameters();
        if (parms.get("username") != null) {
            msg += "<h3>Hello, " + parms.get("username") + "!</h3>";
        }
        return newFixedLengthResponse( msg + "</body></html>\n" );
    }

    public Response about_view(IHTTPSession session){
        String msg = "<html><body><h1>About page of the Demo Server running on an Android</h1>\n";
        Map<String, List<String>> parms = session.getParameters();
        if (parms.get("username") != null) {
            msg += "<h3>Hello, " + parms.get("username") + "!</h3>";
        }
        return newFixedLengthResponse( msg + "</body></html>\n" );
    }

    public Response error_404_view(IHTTPSession session){
        return newFixedLengthResponse("<h1>Page not found!</h1>");
    }
}
