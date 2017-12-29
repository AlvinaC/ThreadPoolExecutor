package threadpoolexec.com.threadpoolexecutor.workerthread;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import threadpoolexec.com.threadpoolexecutor.network.RetrofitInterface;
import threadpoolexec.com.threadpoolexecutor.services.DownloaderService;

/**
 * Gets the reponse from the url
 */
public class Worker implements Runnable {

    private String url;
    private String name;
    private int totalFileSize;
    private Context ctx;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private RetrofitInterface DIinterface;


    public Worker(Context context, RetrofitInterface DIinterface, String url, String name) {
        this.url = url;
        this.name = name;
        this.ctx = context;
        this.DIinterface = DIinterface;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start download = " + name);
        notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(ctx)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(getName())
                .setContentText("Downloading File")
                .setAutoCancel(true);
        int i = Integer.parseInt(getName().split(" ")[1]);
        notificationManager.notify(i, notificationBuilder.build());
        startDownload(i);

    }

    private void startDownload(final int i) {
        Call<ResponseBody> fileDwnld = DIinterface.downloadFile(url);
        fileDwnld.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Reader task = new Reader(ctx, response.body(), i, notificationBuilder, notificationManager);
                ((DownloaderService) ctx).executor.execute(task);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }
}
