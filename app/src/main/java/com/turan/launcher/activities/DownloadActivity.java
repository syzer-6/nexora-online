package com.turan.launcher.activities;

import static com.turan.Config.GAME_PATH;
import static com.turan.Config.PATH_DOWNLOADS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import com.turan.Config;
import com.turan.game.BuildConfig;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.PointerIconCompat;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.akexorcist.roundcornerprogressbar.indeterminate.IndeterminateCenteredRoundCornerProgressBar;
import com.turan.App;
import com.turan.Utils;
import com.turan.game.R;
import com.turan.launcher.Preferences;
import com.hzy.libp7zip.P7ZipApi;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

import es.dmoral.toasty.Toasty;

public class DownloadActivity extends AppCompatActivity {
    private TextView downloadText, downloadPercent;
    private RoundCornerProgressBar downloadProgress;
    private IndeterminateCenteredRoundCornerProgressBar unZipProgress;

    private Handler handler;
    public int downloadId = 0;

    // Ruxsat so'rov kodi
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1000;
    private static final int MANAGE_STORAGE_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setContentView(R.layout.activity_load);
        downloadProgress = findViewById(R.id.progressView);
        unZipProgress = findViewById(R.id.progressBarUnzip);
        downloadText = findViewById(R.id.textView2);
        downloadPercent = findViewById(R.id.textView15);

        // ✅ Ruxsatlarni tekshirish
        checkStoragePermissions();
    }

    // 🔹 Xotira ruxsatlarini tekshirish
    private void checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ uchun
            if (Environment.isExternalStorageManager()) {
                // Ruxsat berilgan
                checkUrlsAndStartDownload();
            } else {
                // MANAGE_EXTERNAL_STORAGE so'rash
                requestManageStoragePermission();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-10 uchun
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Ruxsatlar berilgan
                checkUrlsAndStartDownload();
            } else {
                // Oddiy ruxsatlarni so'rash
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        STORAGE_PERMISSION_REQUEST_CODE);
            }
        } else {
            // Android 5 va past - ruxsat shart emas
            checkUrlsAndStartDownload();
        }
    }

    // 🔹 Android 11+ uchun MANAGE_EXTERNAL_STORAGE so'rash
    private void requestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MANAGE_STORAGE_REQUEST_CODE);
            } catch (Exception e) {
                // Agar sozlamalar sahifasi topilmasa
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, MANAGE_STORAGE_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Ruxsat berilgan
                    checkUrlsAndStartDownload();
                } else {
                    // Ruxsat rad etilgan
                    showErrorDialog("Файловые разрешения не предоставлены. Приложение не может скачивать файлы.");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // Ruxsatlar berilgan
                checkUrlsAndStartDownload();
            } else {
                // Ruxsatlar rad etilgan
                showErrorDialog("Разрешения на хранение не предоставлены. Приложение не может скачивать файлы.");
            }
        }
    }

    // 🔹 URL larni tekshirish va yuklashni boshlash
    private void checkUrlsAndStartDownload() {
        long freeMemory = getFreeMemory();
        if (freeMemory < 3000) {
            handler.postDelayed(() -> {
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.item_dialog_settings);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_dialog_full);
                dialog.getWindow().setLayout(-1, -2);
                ((TextView) dialog.findViewById(R.id.message)).setText("У вас осталось мало памяти, установка невозможна.");
                ((TextView) dialog.findViewById(R.id.ok)).setOnClickListener(view1 -> {
                    view1.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click));
                    handler.postDelayed(() -> {
                        dialog.dismiss();
                        finish();
                    }, 200);
                });
                dialog.show();
            }, 200);
            Utils.writeLog(this, 'i', "мало памяти");
            return;
        }

        // ✅ URL larni tekshirish
        if (App.getInstance().downloadID == null) {
            App.getInstance().downloadID = App.INSTALL_TYPE_GAMEFILES;
        }

        // ✅ URL larni tekshirish
        String downloadUrl = getDownloadUrl(App.getInstance().downloadID);
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            Utils.writeLog(this, 'e', "error, download URL is null or empty for ID: " + App.getInstance().downloadID);
            showErrorDialog("ID=" + App.getInstance().downloadID + " URL_GF=" + App.getInstance().URL_GAME_FILES + " DL=" + getDownloadUrl(App.getInstance().downloadID));
            return;
        }

        Utils.writeLog(this, 'i', "Starting download with URL: " + downloadUrl);
        startDownload(App.getInstance().downloadID);
    }

    // 🔹 Download URL ni olish
    private String getDownloadUrl(int downloadId) {
        switch (downloadId) {
            case 1:
                return App.getInstance().URL_CLIENT;
            case 2:
                String url = App.getInstance().URL_GAME_FILES;
                return (url != null && !url.isEmpty()) ? url : "https://github.com/syzer-6/nexora-online/releases/download/v1.0/com.turan.game.zip";
            case 3:
                return App.getInstance().URL_GAME_FILES_UPDATE;
            default:
                return null;
        }
    }

    // 🔹 Xatolik dialogi
    private void showErrorDialog(String message) {
        handler.postDelayed(() -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.item_dialog_settings);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_dialog_full);
            dialog.getWindow().setLayout(-1, -2);
            ((TextView) dialog.findViewById(R.id.message)).setText(message);
            ((TextView) dialog.findViewById(R.id.ok)).setOnClickListener(view1 -> {
                view1.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click));
                handler.postDelayed(() -> {
                    dialog.dismiss();
                    finish();
                }, 200);
            });
            dialog.show();
        }, 200);
    }

    private long getFreeMemory() {
        try {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            return (statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong()) / 1048576;
        } catch (Exception unused) {
            return 268435455;
        }
    }

    private void startDownload(final int id) {
        try {
            clearDownloadsPath();

            // ✅ URL ni qayta tekshirish
            String downloadUrl = getDownloadUrl(id);
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                Utils.writeLog(this, 'e', "Invalid URL for download ID: " + id);
                Toasty.error(this, "Ошибка: Неверный URL для загрузки", Toast.LENGTH_LONG).show();
                return;
            }

            Utils.writeLog(this, 'i', "Creating download task for ID: " + id + ", URL: " + downloadUrl);
            downloadId = createDownloadTask(id).start();
        } catch (Exception e) {
            Utils.writeLog(DownloadActivity.this, 'e', "Ошибка startDownload: " + e.getMessage());
            Toasty.error(DownloadActivity.this, "Ошибка начала загрузки: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private BaseDownloadTask createDownloadTask(final int position) {
        String url = getDownloadUrl(position);
        String path = getDownloadPath(position);

        if (url == null || url.isEmpty()) {
            Utils.writeLog(this, 'e', "URL is null or empty for position: " + position);
            throw new IllegalStateException("URL is null for position: " + position);
        }

        Utils.writeLog(this, 'i', "Creating download task - Position: " + position + ", URL: " + url + ", Path: " + path);

        return FileDownloader.getImpl().create(url)
                .setPath(path, false)
                .setCallbackProgressTimes(300)
                .setMinIntervalUpdateSpeed(400)
                .setListener(new FileDownloadSampleListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.pending(task, soFarBytes, totalBytes);
                        Utils.writeLog(DownloadActivity.this, 'i', "Download pending: " + task.getUrl());
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.progress(task, soFarBytes, totalBytes);
                        final float percent = soFarBytes / (float) totalBytes;
                        downloadProgress.setMax(100);
                        downloadProgress.setProgress((int) (percent * 100));

                        String progressText = "Загрузка файлов игры...";
                        String notificationText = "Загрузка файлов игры... - " + (int) (percent * 100) + "%\n" +
                                Utils.formatFileSize(soFarBytes) + "/" +
                                Utils.formatFileSize(totalBytes) + " Скорость: " +
                                Utils.formatFileSize(task.getSpeed() * 1024);

                        if (position == 1) {
                            progressText = "Обновляем лаунчер...";
                            notificationText = "Обновляем лаунчер... - " + (int) (percent * 100) + "%\n" +
                                    Utils.formatFileSize(soFarBytes) + "/" +
                                    Utils.formatFileSize(totalBytes) + " Скорость: " +
                                    Utils.formatFileSize(task.getSpeed() * 1024);
                        } else if (position == 3) {
                            progressText = "Обновляем файлы игры...";
                            notificationText = "Обновляем файлы игры... - " + (int) (percent * 100) + "%\n" +
                                    Utils.formatFileSize(soFarBytes) + "/" +
                                    Utils.formatFileSize(totalBytes) + " Скорость: " +
                                    Utils.formatFileSize(task.getSpeed() * 1024);
                        }

                        downloadText.setText(progressText);
                        downloadPercent.setText((int) (percent * 100) + "%");
                        createNotification(notificationText);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        super.error(task, e);
                        Utils.writeLog(DownloadActivity.this, 'e', "Ошибка FileDownloader: " + e);
                        Toasty.error(DownloadActivity.this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_LONG).show();

                        // ✅ Xatolik yuz berganda asosiy oynaga qaytish
                        handler.postDelayed(() -> {
                            Intent intent = new Intent(DownloadActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }, 3000);
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        super.connected(task, etag, isContinue, soFarBytes, totalBytes);
                        Utils.setDownloading(true);
                        Utils.writeLog(DownloadActivity.this, 'i', "Download connected: " + task.getUrl() + ", Total: " + totalBytes);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.paused(task, soFarBytes, totalBytes);
                        Utils.setDownloading(false);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        super.completed(task);
                        Utils.writeLog(DownloadActivity.this, 'i', "Download completed: " + task.getUrl() + ", Path: " + task.getPath());
                        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

                        if (position == 1) {
                            Toasty.info(DownloadActivity.this, "Подтвердите установку").show();
                            Utils.setDownloading(false);
                            installAPK("client");
                        } else if (position == 2) {
                            downloadText.setText("Распаковка файлов...");
                            UnZipZip("game");
                        } else if (position == 3) {
                            downloadText.setText("Распаковка обновления...");
                            UnZipZip("files_upd");
                        }
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        super.warn(task);
                        Utils.writeLog(DownloadActivity.this, 'w', "Download warning: " + task.getUrl());
                    }
                });
    }

    // 🔹 Download fayl yo'lini olish
    private String getDownloadPath(int position) {
        switch (position) {
            case 1:
                return PATH_DOWNLOADS + "client.apk";
            case 2:
                return PATH_DOWNLOADS + "game.zip";
            case 3:
                return PATH_DOWNLOADS + "files_upd.zip";
            default:
                throw new IllegalStateException("Unexpected value: " + position);
        }
    }

    public void UnZipZip(final String zipname) {
        try {
            File tempFile = new File(PATH_DOWNLOADS + zipname + ".zip.temp");
            File finalFile = new File(PATH_DOWNLOADS + zipname + ".zip");
            if (tempFile.exists()) {
                if (finalFile.exists()) finalFile.delete();
                tempFile.renameTo(finalFile);
            }
        } catch (Exception e) {
            Utils.writeLog(this, 'e', "Rename error: " + e.getMessage());
        }
        UnZipTask unzipTask = new UnZipTask(this);
        unzipTask.execute(PATH_DOWNLOADS, Config.GAME_PATH, zipname + ".zip");
    }

    private class UnZipTask extends AsyncTask<String, Integer, Integer> {
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public UnZipTask(Context context) {
            this.context = context;
        }

        @Override
        protected Integer doInBackground(String... params) {
            String filePath = params[0];
            String destinationPath = params[1];
            String fileName = params[2];

            Utils.writeLog(DownloadActivity.this, 'i', "Starting extraction: " + filePath + fileName + " to " + destinationPath);

            try {
                // ✅ 7zip dan natijani olish (int qaytaradi)
                int resultCode = P7ZipApi.executeCommand(UnZip.getExtractCmd(filePath + fileName, destinationPath));
                Utils.writeLog(DownloadActivity.this, 'i', "Extraction result code: " + resultCode);
                return resultCode;
            } catch (Exception e) {
                Utils.writeLog(DownloadActivity.this, 'e', "Extraction error: " + e.getMessage());
                return -1; // Xatolik kodi
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utils.setDownloading(true);
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            downloadPercent.setVisibility(View.GONE);
            downloadProgress.setVisibility(View.GONE);
            unZipProgress.setVisibility(View.VISIBLE);
            downloadText.setText("Идет распаковка файлов игры...");
            createNotification("Идет распаковка файлов игры...");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            // окончание распаковки
            mWakeLock.release();
            clearDownloadsPath();
            Utils.setDownloading(false);
            downloadProgress.setProgress(0);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

            // ✅ 7-zip natija kodlarini tekshirish
            // 0 - muvaffaqiyatli, boshqa kodlar - xatolik
            if (resultCode != 0) {
                Utils.writeLog(DownloadActivity.this, 'e', "Ошибка распаковки. Код: " + resultCode);

                String errorMessage = "Ошибка распаковки";
                switch (resultCode) {
                    case 1: errorMessage = "Предупреждение"; break;
                    case 2: errorMessage = "Критическая ошибка"; break;
                    case 7: errorMessage = "Ошибка командной строки"; break;
                    case 8: errorMessage = "Недостаточно памяти"; break;
                    case 255: errorMessage = "Процесс прерван"; break;
                    default: errorMessage = "Ошибка распаковки. Код: " + resultCode; break;
                }

                Toasty.error(DownloadActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            } else {
                        // Move extracted files from nested folder to correct location
                        File nestedDir = new File(Config.GAME_PATH + "com.turan.game/files/");
                        if (nestedDir.exists()) {
                                copyDirectory(nestedDir, new File(Config.GAME_PATH));
                                deleteDirectory(nestedDir);
                                new File(Config.GAME_PATH + "com.turan.game/").delete();
                        }
                // ✅ Settings faylini yangilash
                File settings = new File(GAME_PATH + "SAMP/settings.ini");
                if (settings.exists()) {
                    try {
                        Wini w = new Wini(settings);
                        w.put("client", "name", Preferences.getString(DownloadActivity.this, Preferences.NICKNAME));
                        w.store();
                        Utils.writeLog(DownloadActivity.this, 'i', "Settings updated successfully");
                    } catch (IOException e) {
                        Utils.writeLog(DownloadActivity.this, 'e', "Ошибка обновления settings: " + e.getMessage());
                    }
                }
                Toasty.success(DownloadActivity.this, "Игра установлена!", Toast.LENGTH_SHORT).show();
            }

            // ✅ Asosiy oynaga o'tish
            Intent intent = new Intent(DownloadActivity.this, MainActivity.class);
            intent.putExtras(getIntent());
            startActivity(intent);
            finish();
        }

        private void mkdirs(File outdir, String path) {
            File d = new File(outdir, path);
            if (!d.exists()) d.mkdirs();
        }

        private String dirpart(String name) {
            int s = name.lastIndexOf(File.separatorChar);
            return s == -1 ? null : name.substring(0, s);
        }
    }

    private void installAPK(String apkname) {
        try {
            File file = new File(PATH_DOWNLOADS, apkname + ".apk");
            Intent intent;
            if (file.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
                    intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setData(apkUri);
                } else {
                    Uri apkUri = Uri.fromFile(file);
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
                // ✅ APK o'rnatilgandan so'ng ilovani yopish
                finish();
            } else {
                Toasty.error(this, "APK файл не найден: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Utils.writeLog(this, 'e', "Ошибка установки:" + e.getMessage());
            Toasty.error(this, "Ошибка установки: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void clearDownloadsPath() {
        File download_path = new File(PATH_DOWNLOADS);
        if (!download_path.exists()) {
            download_path.mkdirs();
            Utils.writeLog(this, 'i', "Downloads directory created: " + PATH_DOWNLOADS);
        } else {
            File client = new File(PATH_DOWNLOADS, "client.apk");
            if (client.exists()) client.delete();
            File launcher = new File(PATH_DOWNLOADS, "launcher.apk");
            if (launcher.exists()) launcher.delete();
            File gameFiles = new File(PATH_DOWNLOADS, "game.zip");
            if (gameFiles.exists()) gameFiles.delete();
                File gameTemp = new File(PATH_DOWNLOADS, "game.zip.temp");
                if (gameTemp.exists()) gameTemp.delete();
            File filesUpd = new File(PATH_DOWNLOADS, "files_upd.zip");
            if (filesUpd.exists()) filesUpd.delete();
            File filesGraph = new File(PATH_DOWNLOADS, "files_graph.zip");
            if (filesGraph.exists()) filesGraph.delete();
        }
    }

    @SuppressLint("RestrictedApi")
    public void createNotification(String str) {
        NotificationManager notifManager = null;
        NotificationCompat.Builder builder = null;
        Intent intent;
        PendingIntent pendingIntent;
        notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notifManager.getNotificationChannel("downloading_channel_1") == null) {
                NotificationChannel notificationChannel = new NotificationChannel("downloading_channel_1", "Загрузка", NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setDescription("Скачивание и распаковка");
                notificationChannel.enableVibration(false);
                notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
                notificationChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setVibrationPattern(new long[]{0});
                notifManager.createNotificationChannel(notificationChannel);
            }
            builder = new NotificationCompat.Builder(this, "downloading_channel_1");
            intent = new Intent(this, getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.setContentTitle("SMART RP MOBILE").setSmallIcon(R.mipmap.ic_launcher_round).setVibrate(new long[]{0}).setContentText(str).setAutoCancel(true).setContentIntent(pendingIntent).setTicker("RUSSIAN STATE").setOnlyAlertOnce(true).setOngoing(true);
        } else {
            intent = new Intent(this, getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentTitle("SMART RP MOBILE").setSmallIcon(R.mipmap.ic_launcher_round).setVibrate(new long[]{0}).setContentText(str).setAutoCancel(true).setContentIntent(pendingIntent).setTicker("RUSSIAN STATE").setOnlyAlertOnce(true).setOngoing(true).setPriority(1);
        }
        notifManager.notify(PointerIconCompat.TYPE_HAND, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        // ✅ Notification ni tozalash
        NotificationManager notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifManager.cancelAll();
    }

    private void copyDirectory(File src, File dst) {
        File[] files = src.listFiles();
        if (files == null) return;
        dst.mkdirs();
        for (File f : files) {
            File target = new File(dst, f.getName());
            if (f.isDirectory()) {
                copyDirectory(f, target);
            } else {
                try {
                    java.io.FileInputStream in = new java.io.FileInputStream(f);
                    java.io.FileOutputStream out = new java.io.FileOutputStream(target);
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    in.close();
                    out.close();
                } catch (Exception e) {
                    Utils.writeLog(DownloadActivity.this, 'e', "Copy failed: " + f.getName());
                }
            }
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) {
            if (f.isDirectory()) deleteDirectory(f);
            else f.delete();
        }
        dir.delete();
    }
}