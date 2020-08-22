package com.iot_rest_application.iothink_unina.utilities;

import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RestRequest extends AsyncTask<String, Integer, String> {


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... strings) {

        StringBuilder textBuilder = new StringBuilder();
        URL url_http = null;
        try {
            url_http = new URL(strings[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try{
            HttpURLConnection urlConnection = (HttpURLConnection) url_http.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());


            try (Reader reader = new BufferedReader(new InputStreamReader
                    (in, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }

            //System.out.println(textBuilder);
        }catch (IOException e){
            e.printStackTrace();
        }


        return textBuilder.toString();
    }
}
