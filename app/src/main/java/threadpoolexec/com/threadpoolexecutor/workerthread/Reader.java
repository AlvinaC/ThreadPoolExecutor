package threadpoolexec.com.threadpoolexecutor.workerthread;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import threadpoolexec.com.threadpoolexecutor.R;
import threadpoolexec.com.threadpoolexecutor.models.DownloadObject;

/**
 * Reads the stream
 * Stores the files in Downloads
 * Displays progress in notification panel
 */
public class Reader implements Runnable {

    private Context ctx;
    private ResponseBody body;
    private int i;
    private int totalFileSize;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    public Reader(Context ctx, ResponseBody body, int i, NotificationCompat.Builder notificationBuilder, NotificationManager notificationManager) {
        this.ctx = ctx;
        this.body = body;
        this.i = i;
        this.notificationBuilder = notificationBuilder;
        this.notificationManager = notificationManager;
    }

    @Override
    public void run() {
        try {
            downloadFile(body, i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(ResponseBody body, int i) throws IOException {
        System.out.println(Thread.currentThread().getName() + " downloadFile(ResponseBody body)");
        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), i + ".zip");
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        String unit;
        while ((count = bis.read(data)) != -1) {
            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));
            unit = "MB";
            if (totalFileSize == 0) {
                totalFileSize = (int) (fileSize / (Math.pow(1024, 1)));
                current = Math.round(total / (Math.pow(1024, 1)));
                unit = "KB";
            }
            if (totalFileSize == 0) {
                totalFileSize = (int) (fileSize);
                current = Math.round(total);
                unit = "B";
            }
            int progress = (int) ((total * 100) / fileSize);
            long currentTime = System.currentTimeMillis() - startTime;
            DownloadObject download = new DownloadObject();
            download.setTotalFileSize(totalFileSize);
            if (currentTime > 1000 * timeCount) {
                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                sendNotification(download, i, unit);
                timeCount++;
            }
            output.write(data, 0, count);
        }
        onDownloadComplete(i);
        output.flush();
        output.close();
        bis.close();
        System.out.println(Thread.currentThread().getName() + " End name");
    }

    private void sendIntent(DownloadObject download) {
        Intent intent = new Intent("MESSAGE_PROGRESS");
        intent.putExtra("download", download);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }

    private void sendNotification(DownloadObject download, int i, String unit) {
        sendIntent(download);
        notificationBuilder.setProgress(100, download.getProgress(), false);
        notificationBuilder.setContentText("Downloading file " + download.getCurrentFileSize() + "/" + totalFileSize + unit);
        notificationManager.notify(i, notificationBuilder.build());
    }

    private void onDownloadComplete(int i) {
        DownloadObject download = new DownloadObject();
        download.setProgress(100);
        sendIntent(download);
        notificationManager.cancel(i);
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setSmallIcon(R.drawable.ic_download);
        notificationBuilder.setContentText("File Downloaded");
        notificationManager.notify(i, notificationBuilder.build());
    }
}
