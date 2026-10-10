package com.turan.launcher.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.turan.Config;
import com.turan.game.R;
import com.turan.launcher.fragments.NewsFragment;
import com.turan.launcher.fragments.SettingsFragment;

import java.util.Timer;
import java.util.TimerTask;

public class MenuFragment extends AppCompatActivity {

    private LinearLayout lin;
    private LinearLayout lin2;
    private LinearLayout lin3;
    private LinearLayout lin4;
    private LinearLayout lin5;
    private LinearLayout lin6;
    private LinearLayout lin7;
    private LinearLayout lin8;
    private LinearLayout lin9;

    private Fragment SettingsFragment;


    private Fragment NewsFragment;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_menu);


        Animation animation = AnimationUtils.loadAnimation(this, R.anim.button_click);

        lin = (LinearLayout) findViewById(R.id.linearLayout);
        lin2 = (LinearLayout) findViewById(R.id.linearLayout2);
        lin3 = (LinearLayout) findViewById(R.id.linearLayout3);
        lin4 = (LinearLayout) findViewById(R.id.linearLayout4);
        lin5 = (LinearLayout) findViewById(R.id.linearLayout5);
        lin6 = (LinearLayout) findViewById(R.id.linearLayout6);
        lin7 = (LinearLayout) findViewById(R.id.linearLayout7);
        lin8 = (LinearLayout) findViewById(R.id.linearLayout8);
        lin9 = (LinearLayout) findViewById(R.id.linearLayout9);

        SettingsFragment = new SettingsFragment();
        NewsFragment = new NewsFragment();












        lin3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MenuFragment.this, "Настройки", Toast.LENGTH_SHORT).show();
                v.startAnimation(animation);
                replaceFragment(SettingsFragment);
            }

        });
        lin2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MenuFragment.this, "NEWS", Toast.LENGTH_SHORT).show();
                v.startAnimation(animation);
                replaceFragments(NewsFragment);
            }

        });


        lin.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        v.startAnimation(animation);
                        onClickPlay();
                    }
                });
        lin4.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(MenuFragment.this, "SITE", Toast.LENGTH_SHORT).show();
                        v.startAnimation(animation);
                        Intent si = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.VK_LINK));
                        startActivity(si);

                    }
                });
        lin5.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(MenuFragment.this, "FORUM", Toast.LENGTH_SHORT).show();
                        v.startAnimation(animation);
                        Intent f = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.VK_LINK));
                        startActivity(f);

                    }
                });
        lin6.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(MenuFragment.this, "DONATE", Toast.LENGTH_SHORT).show();
                        v.startAnimation(animation);
                        Intent donate = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.VK_LINK));
                        startActivity(donate);

                    }
                });
        lin7.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(MenuFragment.this, "VK", Toast.LENGTH_SHORT).show();
                        v.startAnimation(animation);
                        Intent vk = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.VK_LINK));
                        startActivity(vk);

                    }
                });

        lin8.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(MenuFragment.this, "YT", Toast.LENGTH_SHORT).show();
                        v.startAnimation(animation);
                        Intent yt = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.YT_LINK));
                        startActivity(yt);

                    }
                });
        lin9.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(MenuFragment.this, "DS", Toast.LENGTH_SHORT).show();
                        v.startAnimation(animation);
                        Intent ds = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.DS_LINK));
                        startActivity(ds);

                    }
                });
    }


    public void onClickPlay() {

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }





    public void replaceFragment(Fragment fragment) {

        getSupportFragmentManager().beginTransaction();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, SettingsFragment)
                .addToBackStack(null)

                .commit();
    }
    public void replaceFragments(Fragment fragment) {

        getSupportFragmentManager().beginTransaction();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, NewsFragment )
                .addToBackStack(null)

                .commit();
    }





    private void startTimer()
    {
        Timer t = new Timer();
        t.schedule(new TimerTask(){

            @Override
            public void run() {
                onClickPlay();
            }
        }, 200L);
    }

    public void onDestroy() {
        super.onDestroy();

    }

    public void onRestart() {
        super.onRestart();

    }








}