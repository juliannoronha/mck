package com.demoproject.demo.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NbaAPIService {
    
    private final OkHttpClient client;
    private final String apiKey;
    private final String apiHost;
    
    public NbaAPIService(OkHttpClient client, String apiKey, String apiHost) {
        this.client = client;
        this.apiKey = apiKey;
        this.apiHost = apiHost;
    }
    
    public Response getPlayerStatistics(String gameId) throws IOException {
        Request request = new Request.Builder()
                .url("https://api-nba-v1.p.rapidapi.com/players/statistics?game=" + gameId)
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", apiHost)
                .build();
                
        return client.newCall(request).execute();
    }
    
    // Add more API methods as needed
}
