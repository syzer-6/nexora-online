package com.turan.launcher.network;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Api {

    @GET("/turan/api.json")
    Call<Links> getLinks();

    @GET("/turan/servers.json")
    Call<ArrayList<Server>> getServers();

    @GET("/turan/news.json")
    Call<ArrayList<Story>> getStories();
}
