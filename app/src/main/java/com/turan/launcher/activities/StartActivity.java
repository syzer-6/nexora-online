package com.turan.launcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.turan.App;
import com.turan.Utils;
import com.turan.game.R;
import com.turan.launcher.Preferences;

import es.dmoral.toasty.Toasty;

public class StartActivity extends AppCompatActivity
{
    Button download;
    EditText nickName;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_start);
        download = findViewById(R.id.button_update);
        nickName = findViewById(R.id.editTextTextPersonName);

        // ввод ника
        nickName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String obj = nickName.getText().toString();
                if (obj.isEmpty()) {
                    Toasty.warning(this, getResources().getString(R.string.enterNik), Toast.LENGTH_LONG).show();
                } else if (!obj.contains("_")) {
                    Toasty.warning(this, getResources().getString(R.string.mustContains_), Toast.LENGTH_LONG).show();
                } else if (obj.length() < 4) {
                    Toasty.warning(this, getResources().getString(R.string.minLengthNik), Toast.LENGTH_LONG).show();
                } else {
                    Preferences.putString(this, Preferences.NICKNAME, obj);
                    Toasty.success(this, "Ваш никнейм сохранен: "+obj).show();
                    //App.getInstance().downloadID = App.INSTALL_TYPE_GAMEFILES;
                    startActivity(new Intent(StartActivity.this, DownloadActivity.class));
                    finish();
                }
            }
            return false;
        });
        // кнопка "продолжить"
        download.setOnClickListener(view -> {
            String obj = nickName.getText().toString();
            if (obj.isEmpty()) {
                Toasty.warning(StartActivity.this, getResources().getString(R.string.enterNik), Toast.LENGTH_LONG).show();
            } else if (!obj.contains("_")) {
                Toasty.warning(StartActivity.this, getResources().getString(R.string.mustContains_), Toast.LENGTH_LONG).show();
            } else if (obj.length() < 4) {
                Toasty.warning(StartActivity.this, getResources().getString(R.string.minLengthNik), Toast.LENGTH_LONG).show();
            } else {
                Preferences.putString(StartActivity.this, Preferences.NICKNAME, obj);
                Toasty.success(StartActivity.this, "Ваш никнейм сохранен: "+obj).show();
                if(!Utils.isGameInstalled()) {
                    App.getInstance().downloadID = App.INSTALL_TYPE_GAMEFILES;
                    startActivity(new Intent(StartActivity.this, DownloadActivity.class));
                    finish();
                }
            }
        });
    }
}
