package com.example.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

public class CrptApi {
    private final int requestLimit;
    private final long intervalInMillis;
    private final Object lock = new Object();
    private volatile int requestCount = 0;
    private long lastResetTime = System.currentTimeMillis();

    private CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.intervalInMillis = timeUnit.toMillis(1);

    }

    public void creatDocument(Object document, String signature) {
        synchronized (lock) {
            Long currentTime = System.currentTimeMillis();
            if (currentTime - lastResetTime >= intervalInMillis) {
                requestCount = 0;
                lastResetTime = currentTime;
            }
            if (requestCount >= requestLimit) {
                System.out.println("Request limit exceeded. Wait for next interval...");
                return;
            }
            requestCount++;
        }
        try {
            URL url = new URL("https://ismp.crpt.ru/api/v3/lk/documents/create");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);


            Gson gson = new Gson();
            String jsonInputString = gson.toJson(document);
            OutputStream os = connection.getOutputStream();
            os.write(jsonInputString.getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                System.out.println("Document created successfully.");
            }else {
                System.out.println("Failed to create document. Response code: " +responseCode);
            }

        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
           e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 10);
        Object document = new Object();
        String signature = "SampleSignature";
        crptApi.creatDocument(document, signature);
    }
}
