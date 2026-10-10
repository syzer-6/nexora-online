package com.turan.launcher.activities;

import static com.turan.Config.GAME_PATH;
import static com.turan.Config.PATH_DOWNLOADS;
import static es.dmoral.toasty.Toasty.LENGTH_SHORT;

import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.turan.App;
import com.turan.Config;
import com.turan.Utils;
import com.turan.game.R;
import com.turan.launcher.Preferences;
import com.turan.launcher.fragments.ServersFragment;
import com.liulishuo.filedownloader.FileDownloader;
import com.turan.launcher.fragments.SettingsFragment;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity
{
    // Knopkalar
    private ImageButton btnDonate2;

    private EditText nickName;
    private ImageButton btnMenu, btnDonate, btnBonus, btnHelp, btnManabu;
    private LinearLayout bonusBtnLayout, helpBtnLayout, manabuBtnLayout;


    private boolean doubleBackToExitPressedOnce;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        // O'yin o'rnatilganligini tekshirish
        if(!Utils.isGameInstalled())
        {
            App.getInstance().downloadID = App.INSTALL_TYPE_GAMEFILES;
            startActivity(new Intent(MainActivity.this, DownloadActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Knopkalarni topish
        initializeViews();

        // 🔹 Nickni oldindan yuklash
        nickName.setText(Preferences.getString(this, Preferences.NICKNAME));

// 🔹 Nick kiritilganda saqlash
        nickName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String obj = nickName.getText().toString().trim();

                if (obj.isEmpty()) {
                    Toasty.warning(this, getString(R.string.enterNik), Toast.LENGTH_LONG).show();
                    return true;
                }

                if (!obj.contains("_")) {
                    Toasty.warning(this, getString(R.string.mustContains_), Toast.LENGTH_LONG).show();
                    return true;
                }

                if (obj.length() < 4) {
                    Toasty.warning(this, getString(R.string.minLengthNik), Toast.LENGTH_LONG).show();
                    return true;
                }

                // 🔹 Preferences ga saqlash
                Preferences.putString(this, Preferences.NICKNAME, obj);
                Toasty.success(this, "Ник сохранён: " + obj).show();

                // 🔹 settings.ini ga yozish
                File settings = new File(GAME_PATH + "SAMP/settings.ini");
                if (settings.exists()) {
                    try {
                        Wini w = new Wini(settings);
                        w.put("client", "name", obj);
                        w.store();
                    } catch (IOException e) {
                        Utils.writeLog(this, 'e', "Ошибка: " + e.getMessage());
                    }
                } else {
                    Utils.writeLog(this, 'e', "settings.ini topilmadi");
                }
                return true;
            }
            return false;
        });


        // Animation yuklash
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.button_click);

        // Yuklash papkasini yaratish
        File download_path = new File(PATH_DOWNLOADS);
        if(!download_path.exists()) {
            download_path.mkdirs();
        }

        // Asosiy fragmentni yuklash
        loadFragment(new ServersFragment());

        // Knopkalarga click listenerlar qo'shish
        setupClickListeners(animation);
    }

    // 🔹 Knopkalarni topish
    private void initializeViews() {
        btnMenu = findViewById(R.id.menu);
        btnDonate = findViewById(R.id.btnDonate);
        btnBonus = findViewById(R.id.bonus_btn_icon);
        btnHelp = findViewById(R.id.imageButton5);
        btnManabu = findViewById(R.id.button5);

        nickName = findViewById(R.id.editTextMainNick); // 🔹 QO‘SHILDI
        btnDonate2 = findViewById(R.id.btnDonate2);

        bonusBtnLayout = findViewById(R.id.bonus_btn);
        helpBtnLayout = findViewById(R.id.help_btn);
        //manabuBtnLayout = findViewById(R.id.manabu_btn);
    }

    // 🔹 Click listenerlarni sozlash
    private void setupClickListeners(Animation animation) {
        // BONUS knopkasi
        if (btnBonus != null) {
            btnBonus.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "BONUS", Toast.LENGTH_SHORT).show();
                    v.startAnimation(animation);
                    Intent vk = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.VK_LINK));
                    startActivity(vk);
                }
            });
        }

        if (btnDonate2 != null) {
            btnDonate2.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Donate", Toast.LENGTH_SHORT).show();
                v.startAnimation(animation);
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://turan-rp.uz/donate/"));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toasty.error(MainActivity.this, "Ошибка открытия страницы доната", Toast.LENGTH_SHORT).show();
                }
            });
        }


        // HELP knopkasi
        if (btnHelp != null) {
            btnHelp.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    v.startAnimation(animation);
                    Utils.handleServerPlay(
                            MainActivity.this,
                            MainActivity.this,
                            1
                    );
                }
            });
        }


        if (btnMenu != null) {
            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Меню", Toast.LENGTH_SHORT).show();
                    v.startAnimation(animation);

                    // SettingsFragment ni ochish
                    replaceFragment(new SettingsFragment());
                }
            });
        }


        // DONATE knopkasi
        if (btnDonate != null) {
            btnDonate.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Donate", Toast.LENGTH_SHORT).show();
                    v.startAnimation(animation);
                    // Donate sahifasiga o'tish
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://turan-rp.uz/")); // URL ni o'zgartiring
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        Toasty.error(MainActivity.this, "Ошибка открытия страницы доната", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // LinearLayout lar ham ishlashi uchun
        if (bonusBtnLayout != null) {
            bonusBtnLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Бонусы", Toast.LENGTH_SHORT).show();
                    v.startAnimation(animation);
                    Intent vk = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.VK_LINK));
                    startActivity(vk);
                }
            });
        }

        if (helpBtnLayout != null) {
            helpBtnLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    v.startAnimation(animation);
                    Utils.handleServerPlay(
                            MainActivity.this,
                            MainActivity.this,
                            1
                    );
                }
            });
        }

        if (btnManabu != null) {
            btnManabu.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    v.startAnimation(animation);
                    Utils.handleServerPlay(
                            MainActivity.this,
                            MainActivity.this,
                            1
                    );
                }
            });
        }

        if (manabuBtnLayout != null) {
            manabuBtnLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    v.startAnimation(animation);
                    Utils.handleServerPlay(
                            MainActivity.this,
                            MainActivity.this,
                            1
                    );
                }
            });
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_place, fragment) // fragment_place - container ID
                .addToBackStack(null)
                .commit();
    }

    // 🔹 Orqaga bosilganda
    @Override
    public void onBackPressed()
    {
        if (doubleBackToExitPressedOnce) {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
            FileDownloader.getImpl().pauseAll();
            super.onBackPressed();
            return;
        }

        Toasty.info(MainActivity.this, "Нажмите снова, чтобы выйти", LENGTH_SHORT).show();
        doubleBackToExitPressedOnce = true;
        handler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    // 🔹 Fragment yuklash
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_place, fragment);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}