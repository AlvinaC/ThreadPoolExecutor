package threadpoolexec.com.threadpoolexecutor.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import threadpoolexec.com.threadpoolexecutor.R;
import threadpoolexec.com.threadpoolexecutor.models.Events;
import threadpoolexec.com.threadpoolexecutor.network.RetrofitInterface;
import threadpoolexec.com.threadpoolexecutor.util.Constants;
import threadpoolexec.com.threadpoolexecutor.util.CustomApplication;
import threadpoolexec.com.threadpoolexecutor.workerthread.Worker;

/**
 * Service creates a pool and transfers the download job to individual threads
 * Threads run in parallel
 */
public class DownloaderService extends Service {

    private static final String TAG = DownloaderService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 12345678;

    private static final String CHANNEL_ID = "channel_01";

    private ArrayList<String> urls = new ArrayList<>();
    private RetrofitInterface DIinterface;
    public ThreadPoolExecutor executor;
    private NotificationManager mNotificationManager;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    Retrofit retrofit;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        ((CustomApplication) getApplication()).getNetworkComponent().inject(this);
        DIinterface = retrofit.create(RetrofitInterface.class);
        //pool for our downloads
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        registerEventBus();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service onBind");
        stopForeground(true);
        return new LocalBinder<DownloaderService>(this);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "Service onRebind");
        stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Service onUnbind");
        if (CustomApplication.requestingUpdates(this)) {
            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        Log.d(TAG, "Service onStartCommand");
        getUrls();
        CustomApplication.setRequestingUpdates(this, true);
        for (int i = 0; i < urls.size(); i++) {
            Worker task = new Worker(this, DIinterface, urls.get(i), "Worker " + i);
            System.out.println("A new task has been added : " + task.getName());
            try {
                if (!executor.isTerminating())
                    executor.execute(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void getUrls() {
        urls.clear();
        urls.add(Constants.url1);
        urls.add(Constants.url2);
        urls.add(Constants.url3);
        urls.add(Constants.url4);
        urls.add(Constants.url5);
    }

    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        super.onDestroy();
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, DownloaderService.class);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText("Download continued...")
                .setContentTitle(getResources().getString(R.string.app_name))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Downloads from ThreadPoolExecutor still running...")
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    private void registerEventBus() {
        disposables.add(((CustomApplication) getApplication())
                .bus()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object object) throws Exception {
                        if (object instanceof Events.CompleteEvent) {
                            Log.d(TAG, "active task count :" + executor.getActiveCount() + " completed :" + executor.getCompletedTaskCount());
                            if (executor.getActiveCount() == 0) {
                                Log.d(TAG, "inside active task count 0");
                                awaitTerminationAfterShutdown(executor);
                                stopForeground(true);
                                stopSelf();
                                CustomApplication.setRequestingUpdates(DownloaderService.this, false);
                            }
                        }
                    }
                }));

    }

    public void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
