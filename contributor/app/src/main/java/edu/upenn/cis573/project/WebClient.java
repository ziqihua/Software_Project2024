package edu.upenn.cis573.project;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class WebClient {

    private String host;
    private int port;

    public WebClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public WebClient() {

    }

    /**
     * Make an HTTP request to the RESTful API at the object's host:port
     * The request will be of the form http://[host]:[port]/[resource]?
     * followed by key=value& for each of the entries in the queryParams map.
     * @return the JSON object returned by the API if successful, null if unsuccessful
     */
    public String makeRequest(String resource, Map<String, Object> queryParams) {

        //Log.v("webclient", request);

        /*
        Web traffic must be done in a background thread in Android.
        */
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit( () -> {
                String response = null;
                try {
                    String request = "http://" + host + ":" + port + resource + "?";

                    for (String key : queryParams.keySet()) {
                        request += key + "=" + queryParams.get(key) + "&";

                    }
                    URL url = new URL(request);
                    url.openConnection();
                    Scanner in = new Scanner(url.openStream());
                    response = "";
                    while (in.hasNext()) {
                        String line = in.nextLine();
                        response += line;
                    }

                    in.close();

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return response;
            }
            );

            return future.get();

        }
        catch (Exception e) {
            // uh oh
            e.printStackTrace();
            return null;
        }
        
    }


}
