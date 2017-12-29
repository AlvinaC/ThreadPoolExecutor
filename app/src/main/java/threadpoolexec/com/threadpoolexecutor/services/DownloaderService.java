package threadpoolexec.com.threadpoolexecutor.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import retrofit2.Retrofit;
import threadpoolexec.com.threadpoolexecutor.network.RetrofitInterface;
import threadpoolexec.com.threadpoolexecutor.util.Constants;
import threadpoolexec.com.threadpoolexecutor.util.CustomApplication;
import threadpoolexec.com.threadpoolexecutor.workerthread.Worker;

/**
 * Service creates a pool and transfers the download job to individual threads
 * Threads run in parallel
 */
public class DownloaderService extends Service {

    //injected object
    @Inject
    Retrofit retrofit;

    private ArrayList<String> urls = new ArrayList<>();
    private RetrofitInterface DIinterface;
    public ThreadPoolExecutor executor;

    @Override
    public void onCreate() {
        super.onCreate();
        ((CustomApplication) getApplication()).getNetworkComponent().inject(this);
        DIinterface = retrofit.create(RetrofitInterface.class);
        //pool for our downloads
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder<DownloaderService>(this);
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
        //when app process is killed, the service starts on its own
        return Service.START_STICKY;
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
        executor.shutdown();
    }
}
