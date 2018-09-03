package threadpoolexec.com.threadpoolexecutor.workerthread

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import okhttp3.ResponseBody
import threadpoolexec.com.threadpoolexecutor.R
import threadpoolexec.com.threadpoolexecutor.models.DownloadObject
import threadpoolexec.com.threadpoolexecutor.models.Events
import threadpoolexec.com.threadpoolexecutor.services.DownloaderService
import threadpoolexec.com.threadpoolexecutor.util.CustomApplication
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Reads the stream
 * Stores the files in Downloads
 * Displays progress in notification panel
 */
class Reader(private val ctx: Context, private val body: ResponseBody, private val i: Int, private val notificationBuilder: NotificationCompat.Builder, private val notificationManager: NotificationManager) : Runnable {
    private var totalFileSize: Int = 0

    override fun run() {
        try {
            downloadFile(body, i)
        } catch (e: Exception) {
            e.printStackTrace()
            executeForAll(i, "Error downloading...")
        }

    }

    @Throws(Exception::class)
    private fun downloadFile(body: ResponseBody, i: Int) {
        println(Thread.currentThread().name + " downloadFile(ResponseBody body)")
        var count: Int? = null
        val data = ByteArray(1024 * 4)
        val fileSize = body.contentLength()
        val bis = BufferedInputStream(body.byteStream(), 1024 * 8)
        val outputFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), i.toString() + ".zip")
        val output = FileOutputStream(outputFile)
        var total: Long? = 0
        val startTime = System.currentTimeMillis()
        var timeCount = 1
        var unit: String
        while ({ count = bis.read(data);count }() != -1) {
            total = count?.toLong()?.let { total?.plus(it) }
            totalFileSize = (fileSize / Math.pow(1024.0, 2.0)).toInt()
            var current = total?.div( Math.pow(1024.0, 2.0))?.let { Math.round(it).toDouble() }
            unit = "MB"
            if (totalFileSize == 0) {
                totalFileSize = (fileSize / Math.pow(1024.0, 1.0)).toInt()
                current = total?.div(Math.pow(1024.0, 1.0))?.let { Math.round(it).toDouble() }
                unit = "KB"
            }
            if (totalFileSize == 0) {
                totalFileSize = fileSize.toInt()
                current = total?.toFloat()?.let { Math.round(it).toDouble() }
                unit = "B"
            }
            val progress = ((total?.times(100))?.div( fileSize))?.toInt()
            val currentTime = System.currentTimeMillis() - startTime
            val download = DownloadObject()
            download.totalFileSize = totalFileSize
            if (currentTime > 1000 * timeCount) {
                download.currentFileSize = current!!.toInt()
                download.progress = progress!!
                sendNotification(download, i, unit)
                timeCount++
            }
            count?.let { output.write(data, 0, it) }
        }
        onDownloadComplete(i)
        output.flush()
        output.close()
        bis.close()
        println(Thread.currentThread().name + " End name")
    }

    private fun sendIntent(download: DownloadObject) {
        val intent = Intent("MESSAGE_PROGRESS")
        intent.putExtra("download", download)
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent)
    }

    private fun sendNotification(download: DownloadObject, i: Int, unit: String) {
        sendIntent(download)
        notificationBuilder.setProgress(100, download.progress, false)
        notificationBuilder.setContentText("Downloading file " + download.currentFileSize + "/" + totalFileSize + unit)
        notificationManager.notify(i, notificationBuilder.build())
    }

    private fun onDownloadComplete(i: Int) {
        val download = DownloadObject()
        download.progress = 100
        sendIntent(download)
        executeForAll(i, "File downloaded...")
    }

    private fun executeForAll(i: Int, message: String) {
        notificationManager.cancel(i)
        notificationBuilder.setProgress(0, 0, false)
        notificationBuilder.setSmallIcon(R.drawable.ic_download)
        notificationBuilder.setContentText(message)
        notificationManager.notify(i, notificationBuilder.build())
        ((ctx as DownloaderService).application as CustomApplication).bus()?.send(Events.CompleteEvent())
    }
}