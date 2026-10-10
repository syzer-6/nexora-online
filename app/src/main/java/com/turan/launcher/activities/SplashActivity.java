package com.turan.launcher.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.turan.App;
import com.turan.Utils;
import com.turan.game.BuildConfig;
import com.turan.game.R;
import com.turan.launcher.Preferences;
import com.turan.launcher.fragments.UpdateClient;
import com.turan.launcher.network.ApiService;
import com.turan.launcher.network.Links;
import com.turan.launcher.network.Server;
import com.turan.launcher.network.Story;
import com.google.firebase.messaging.FirebaseMessaging;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    // permissions
    private static final int MAIN_PERMISSIONS_REQUEST_CODE = 300;
    private Handler handler;
    String[] main_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler = new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Utils.writeLog(this, 'i', "app sucessfully started!");
                Init();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, MAIN_PERMISSIONS_REQUEST_CODE);
            }
        } else {
            if (EasyPermissions.hasPermissions(this, main_permissions)) {
                Utils.writeLog(this, 'i', "app sucessfully started!");
                Init();
            } else {
                Utils.writeLog(this, 'i', "app not have full permissions!!!");
                EasyPermissions.requestPermissions(this,
                        "приложению требуется разрешение на запись в память",
                        MAIN_PERMISSIONS_REQUEST_CODE, main_permissions);
            }
        }
    }
    private void Init()
    {
        FileDownloader.setup(this);
        if (!Preferences.getBoolean(this, Preferences.FIRST_START)) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Utils.writeLog(SplashActivity.this, 'e', "FCM Reg failed: "+task.getException());
                    return;
                }
                Preferences.putString(App.getInstance(), Preferences.USER_FCM_KEY, task.getResult());
                Utils.writeLog(SplashActivity.this, 'i', "FCM reg success! Token: "+task.getResult());
            });
            Preferences.putBoolean(SplashActivity.this, Preferences.FIRST_START, true);
            loadAPI();
        }
        else {
            loadAPI();
        }
    }

    // загрузка апи, новостей, серверов, итд
    private void loadAPI() {
        ApiService.getInstance().getApiService().getLinks().enqueue(new Callback<Links>() {
            public void onResponse(Call<Links> call, Response<Links> response) {
                if(response.isSuccessful())
                {
                    if(response.body() != null) {
                        // версии
                        App.getInstance().targetClientVersion = response.body().getTargetClientVersion();
                        App.getInstance().targetGameFilesVersion = response.body().getTargetGameFilesVersion();
                        // ссылки на файлы
                        App.getInstance().URL_CLIENT = response.body().getUrlClient();
                        App.getInstance().URL_GAME_FILES = response.body().getUrlFiles();
                        App.getInstance().URL_GAME_FILES_UPDATE = response.body().getUrlFilesUpdate();
                        // ресурсы проекта
                        App.getInstance().URL_FORUM = response.body().getUrlForum();
                        App.getInstance().URL_DONATE = response.body().getUrlDonate();
                        // соц сети
                        App.getInstance().URL_VK = response.body().getUrlVKontakte();
                        App.getInstance().URL_DISCORD = response.body().getUrlDiscord();
                        App.getInstance().URL_YOUTUBE = response.body().getUrlYoutube();
                        // загрузка остального
                        loadServers();
                    }
                } else {
                    Toasty.error(SplashActivity.this, "Не получается установить соединение с сервером BRILLIANT GAMES. Попробуйте позднее.", Toasty.LENGTH_LONG).show();
                    Utils.writeLog(SplashActivity.this, 'e', "Ошибка загрузки списка API: "+response.errorBody());
                }
            }
            public void onFailure(Call<Links> call, Throwable th) {
                Utils.writeLog(SplashActivity.this, 'e', "Ошибка загрузки списока API: "+th.getMessage());
                if(!Utils.isInternetConnected(SplashActivity.this))
                {
                    Toasty.error(SplashActivity.this, "У вас нет подключения к интернету.", Toasty.LENGTH_LONG).show();
                    return;
                }
                Toasty.error(SplashActivity.this, "Ошибка подключения к серверу. Сервер недоступен.", Toasty.LENGTH_LONG).show();
            }
        });
    }
    private void loadServers() {
        ApiService.getInstance().getApiService().getServers().enqueue(new Callback<ArrayList<Server>>() {
            public void onResponse(Call<ArrayList<Server>> call, Response<ArrayList<Server>> response) {
                if(response.isSuccessful())
                {
                    if (response.body() != null) {
                        App.getInstance().serverList.clear();
                        App.getInstance().serverList.addAll(response.body());
                        if (App.getInstance().serverListener != null) {
                            App.getInstance().serverListener.onChange();
                        }
                        loadStories();
                    }
                } else {
                    Utils.writeLog(SplashActivity.this, 'e', "Ошибка загрузки списка серверов: "+response.errorBody());
                }
            }
            public void onFailure(Call<ArrayList<Server>> call, Throwable th) {
                Utils.writeLog(SplashActivity.this, 'e', "Ошибка загрузки списока серверов: "+th.getMessage());
                if(!Utils.isInternetConnected(SplashActivity.this))
                {
                    Toasty.error(SplashActivity.this, "У вас нет подключения к интернету.", Toasty.LENGTH_LONG).show();
                    return;
                }
                Toasty.error(SplashActivity.this, "Ошибка подключения к серверу. Сервер недоступен.", Toasty.LENGTH_LONG).show();
            }
        });
    }

    private void loadStories() { // загрузка новостей
        ApiService.getInstance().getApiService().getStories().enqueue(new Callback<ArrayList<Story>>() {
            public void onResponse(Call<ArrayList<Story>> call, Response<ArrayList<Story>> response) {
                if(response.isSuccessful()) {
                    if (response.body() != null) {
                        App.getInstance().stories.clear();
                        App.getInstance().stories.addAll(response.body());
                        runOnUiThread(() -> startLauncher());
                    }
                } else {
                    Utils.writeLog(SplashActivity.this, 'e', "Ошибка загрузки списка новостей: "+response.errorBody());
                }
            }
            public void onFailure(Call<ArrayList<Story>> call, Throwable th) {
                Utils.writeLog(SplashActivity.this, 'e', "Ошибка загрузки списока новостей: "+th.getMessage());
                if(!Utils.isInternetConnected(SplashActivity.this))
                {
                    Toasty.error(SplashActivity.this, "У вас нет подключения к интернету.", Toasty.LENGTH_LONG).show();
                    return;
                }
                Toasty.error(SplashActivity.this, "Ошибка подключения к серверу. Сервер недоступен.", Toasty.LENGTH_LONG).show();
            }
        });
    }

    //-------------------------------------------------------------------

    private void startLauncher() {
        if(/*!BuildConfig.DEBUG && */App.getInstance().targetClientVersion != BuildConfig.VERSION_CODE) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new UpdateClient());
            transaction.commit();
            return;
        }
        handler.postDelayed((Runnable) () -> {
            Intent intent = new Intent(this, MainActivity.class);
            if(Preferences.getString(App.getInstance(), Preferences.NICKNAME).isEmpty())
            {
                intent = new Intent(this, StartActivity.class);
            }
            intent.putExtras(getIntent());
            startActivity(intent);
            finish();
        }, 1000);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAIN_PERMISSIONS_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Init();
                } else {
                    Toasty.error(SplashActivity.this, "Приложению необходимо иметь все разрешения, которое оно запраширует!!!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    public void onPermissionsGranted(int requestCode, List<String> list) {
        if(requestCode == MAIN_PERMISSIONS_REQUEST_CODE) {
            Init();
        }
    }
    public void onPermissionsDenied(int requestCode, List<String> list) {
        if(requestCode == MAIN_PERMISSIONS_REQUEST_CODE) {
            Utils.writeLog(SplashActivity.this, 'i', "ошибка выдачи разрешений");
            Toasty.error(SplashActivity.this, "Приложению необходимо иметь все разрешения, которое оно запрашивает!!!", Toast.LENGTH_LONG).show();
            //finish();
        }
    }
}