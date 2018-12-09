package com.example.s1616573.coinz;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.stream.Collectors;


public class DownloadFileTask extends AsyncTask<String, Void, String> {
    DownloadCompleteListener listener = null;

    @Override
    protected String doInBackground(String... urls) {
        try {
            return loadFileFromNetwork(urls[0]);
        } catch (IOException e) {
            String tag = "DownloadFileTask";
            Log.d(tag,"[doInBackground] Unable to load content. Check your network connection");
            return null;
        }
    }

    private String loadFileFromNetwork(String urlString) throws IOException {
        return readStream(downloadUrl(new URL(urlString)));
    }

    // Given a string respresentation of a URL, sets up a connection and gets an input stream.
    private InputStream downloadUrl(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000); // milliseconds
        conn.setConnectTimeout(15000); // milliseconds
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    @NonNull
    private String readStream(InputStream stream) throws IOException {
        // Read input from stream, build result as a string
        // http://www.adam-bien.com/roller/abien/entry/reading_inputstream_into_string_with
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
       // DownloadCompleteRunner.downloadComplete(result);
        listener.downloadComplete(result);
    }
}
