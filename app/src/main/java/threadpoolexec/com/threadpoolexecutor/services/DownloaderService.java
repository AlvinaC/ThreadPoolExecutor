package threadpoolexec.com.threadpoolexecutor.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import threadpoolexec.com.threadpoolexecutor.MainActivity;
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

    private static final int NOTIFICATION_ID = 12345678;

    private static final String EXTRA_STARTED_FROM_NOTIFICATION = "threadpoolexecutor" +
            ".started_from_notification";

    private static final String CHANNEL_ID = "channel_01";

    //injected object
    @Inject
    Retrofit retrofit;

    private ArrayList<String> urls = new ArrayList<>();
    private RetrofitInterface DIinterface;
    public ThreadPoolExecutor executor;
    private NotificationManager mNotificationManager;
    private final CompositeDisposable disposables = new CompositeDisposable();


    @Override
    public void onCreate() {
        super.onCreate();

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
        stopForeground(true);
        return new LocalBinder<DownloaderService>(this);
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (executor.getTaskCount() > 0) {
            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        getUrls();
        for (int i = 0; i < urls.size(); i++) {
            Worker task = new Worker(this, DIinterface, urls.get(i), "Worker " + i);
            System.out.println("A new task has been added : " + task.getName());
            executor.execute(task);
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
        super.onDestroy();
        //shut the pool
        //executor.shutdown();
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, DownloaderService.class);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

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
                            if (!(executor.getTaskCount() > 0))
                                stopSelf();
                        }
                    }
                }));

    }
}
