package threadpoolexec.com.threadpoolexecutor.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import threadpoolexec.com.threadpoolexecutor.R
import threadpoolexec.com.threadpoolexecutor.models.Events
import threadpoolexec.com.threadpoolexecutor.network.RetrofitInterface
import threadpoolexec.com.threadpoolexecutor.util.Constants
import threadpoolexec.com.threadpoolexecutor.util.CustomApplication
import threadpoolexec.com.threadpoolexecutor.workerthread.Worker
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DownloaderService : Service() {

    private val urls = ArrayList<String>()
    private var retrofitInterface: RetrofitInterface? = null
    var executor: ThreadPoolExecutor? = null
    private val mNotificationManager: NotificationManager? = null
    private val disposables = CompositeDisposable()

    @Inject
    lateinit var retrofit: Retrofit

    private val notification: Notification
        get() {
            val builder = NotificationCompat.Builder(this)
                    .setContentText("Download continued...")
                    .setContentTitle(resources.getString(R.string.app_name))
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("Downloads from ThreadPoolExecutor still running...")
                    .setWhen(System.currentTimeMillis())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(CHANNEL_ID)
            }
            return builder.build()
        }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)

            // Create the channel for the notification
            val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager?.createNotificationChannel(mChannel)
        }

        (application as CustomApplication).networkComponent?.inject(this)
        retrofitInterface = retrofit.create(RetrofitInterface::class.java)
        //pool for our downloads
        executor = Executors.newCachedThreadPool() as ThreadPoolExecutor
        registerEventBus()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Service onBind")
        stopForeground(true)
        return LocalBinder(this)
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "Service onRebind")
        stopForeground(true)
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "Service onUnbind")
        if (CustomApplication.requestingUpdates(this)) {
            startForeground(NOTIFICATION_ID, notification)
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }


    override fun onStartCommand(intent: Intent, flags: Int,
                                startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        getUrls()
        CustomApplication.setRequestingUpdates(this, true)
        for (i in urls.indices) {
            val task = Worker(this, retrofitInterface!!, urls[i], "Worker $i")
            println("A new task has been added : " + task.name)
            try {
                if (!executor?.isTerminating!!)
                    executor?.execute(task)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return Service.START_NOT_STICKY
    }

    private fun getUrls() {
        urls.clear()
        urls.add(Constants.url1)
        urls.add(Constants.url2)
        urls.add(Constants.url3)
        urls.add(Constants.url4)
        urls.add(Constants.url5)
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        executor?.let { awaitTerminationAfterShutdown(it) }
        super.onDestroy()
    }

    private fun registerEventBus() {
        (application as CustomApplication)
                .bus()?.toObservable()?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe { `object` ->
                    if (`object` is Events.CompleteEvent) {
                        completeTaskCount++
                        if (completeTaskCount == 5) {
                            completeTaskCount = 0
                            stopForeground(true)
                            stopSelf()
                            CustomApplication.setRequestingUpdates(this@DownloaderService, false)
                        }
                    }
                }?.let { disposables.add(it) }
    }

    private fun awaitTerminationAfterShutdown(threadPool: ExecutorService) {
        threadPool.shutdown()
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow()
            }
        } catch (ex: InterruptedException) {
            threadPool.shutdownNow()
            Thread.currentThread().interrupt()
        }

    }

    companion object {

        private val TAG = DownloaderService::class.simpleName

        private const val NOTIFICATION_ID = 12345678

        private const val CHANNEL_ID = "channel_01"

        private var completeTaskCount = 0
    }
}