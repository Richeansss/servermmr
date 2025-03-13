package org.example.servermmr.controller;

import org.example.servermmr.model.Player;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import java.util.List;

public interface ApiService {
    @POST("/players/register")
    Call<Player> registerPlayer(@Query("name") String name);

    @POST("/players/battle")
    Call<String> startBattle();

    @GET("/players/ranking")
    Call<List<Player>> getRanking();
}

