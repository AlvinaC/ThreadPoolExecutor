package threadpoolexec.com.threadpoolexecutor.workerthread

import android.app.NotificationManager
import android.content.Context
import android.support.v4.app.NotificationCompat

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import threadpoolexec.com.threadpoolexecutor.R
import threadpoolexec.com.threadpoolexecutor.models.Events
import threadpoolexec.com.threadpoolexecutor.network.RetrofitInterface
import threadpoolexec.com.threadpoolexecutor.services.DownloaderService
import threadpoolexec.com.threadpoolexecutor.util.CustomApplication

/**
 * Gets the reponse from the url
 */
class Worker(private val ctx: Context, private val DIinterface: RetrofitInterface, private val url: String, val name: String) : Runnable {
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null

    override fun run() {
        println(Thread.currentThread().name + " Start download = " + name)
        notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(ctx)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(name)
                .setContentText("Downloading File")
                .setAutoCancel(true)
        val i = Integer.parseInt(name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
        notificationManager?.notify(i, notificationBuilder?.build())
        startDownload(i)

    }

    private fun startDownload(i: Int) {
        val fileDwnld = DIinterface.downloadFile(url)
        fileDwnld.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                println(Thread.currentThread().name + " End name")
                val task = Reader(ctx, response.body()!!, i, notificationBuilder!!, notificationManager!!)
                try {
                    (ctx as DownloaderService).executor?.execute(task)
                } catch (e: Exception) {
                    notificationManager?.cancel(i)
                    notificationBuilder?.setProgress(0, 0, false)
                    notificationBuilder?.setSmallIcon(R.drawable.ic_download)
                    notificationBuilder?.setContentText("Error downloading")
                    notificationManager?.notify(i, notificationBuilder?.build())
                    e.printStackTrace()
                    ((ctx as DownloaderService).application as CustomApplication).bus()?.send(Events.CompleteEvent())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }
}
