package com.turan.launcher.activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.turan.game.R;
import com.turan.launcher.network.Movie;
import com.turan.launcher.network.MovieAdapter;

import java.util.ArrayList;
import java.util.List;

public class FaqAct extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Movie> movieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_help);

        recyclerView = findViewById(R.id.faq_recycler);


        ImageButton btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        initData();
        initRecyclerView();
    }

    private void initRecyclerView() {
        MovieAdapter movieAdapter = new MovieAdapter(movieList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(movieAdapter);
    }

    private void initData() {
        movieList = new ArrayList<>();
        onEnterAnimationComplete();

        movieList.add(new Movie("BRILLIANT RP",
                "BRILLIANT ROLE PLAY"));

        movieList.add(new Movie("BRILLIANT RP",
                "BRILLIANT MOBILE — УДОБНЫЙ И СТАБИЛЬНЫЙ ЛАУНЧЕР. ПРОСТОЙ ЗАПУСК, КРАСИВЫЙ ИНТЕРФЕЙС И ВЫСОКАЯ НАДЁЖНОСТЬ."));

        movieList.add(new Movie("BRILLIANT RP",
                "КОМАНДА BRILLIANT RP."));

        movieList.add(new Movie("Brilliant Mobile",
                "ВСЁ, ЧТО НУЖНО ДЛЯ СОВРЕМЕННОГО ПРОЕКТА — В ОДНОМ ЛАУНЧЕРЕ. ПРОСТО, СТИЛЬНО, НАДЁЖНО."));
    }
}
