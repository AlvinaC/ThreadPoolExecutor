package threadpoolexec.com.threadpoolexecutor

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import threadpoolexec.com.threadpoolexecutor.services.DownloaderService
import threadpoolexec.com.threadpoolexecutor.services.LocalBinder
import threadpoolexec.com.threadpoolexecutor.util.Constants
import threadpoolexec.com.threadpoolexecutor.util.CustomApplication

class MainActivity : AppCompatActivity(), View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private var btnStartDwnld: Button? = null

    private var i: Intent? = null

    var mBounded: Boolean = false
    var mService: DownloaderService? = null
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service onServiceConnected")
            }
            mService = (service as LocalBinder<DownloaderService>).service
            mBounded = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) Log.v(TAG, "Service disconnect")
            mService = null
            mBounded = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        setListener()
        if (!checkPermissions())
            requestPermissions()
    }

    private fun setListener() {
        btnStartDwnld?.setOnClickListener(this)
    }

    private fun init() {
        btnStartDwnld = findViewById<View>(R.id.btn_download) as Button
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
        setButtonsState(CustomApplication.requestingUpdates(this))
        doBindService()
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting and binding service")
        }

    }

    override fun onStop() {
        super.onStop()

        if (mBounded) {
            unbindService(mConnection)
            mBounded = false
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun doBindService() {
        i = Intent(this, DownloaderService::class.java)
        bindService(i, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onClick(view: View) {
        startService(i)
    }

    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                    findViewById<View>(R.id.coordinatorLayout),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, object : View.OnClickListener {
                        override fun onClick(view: View) {
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSIONS_REQUEST_CODE)
                        }
                    })
                    .show()
        } else {
            Log.i(TAG, "Requesting permission")
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> setButtonsState(false)
                else -> {
                    setButtonsState(true)
                    Snackbar.make(
                            findViewById<View>(R.id.coordinatorLayout),
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.settings, object : View.OnClickListener {
                                override fun onClick(view: View) {
                                    // Build intent that displays the App settings screen.
                                    val intent = Intent()
                                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    val uri = Uri.fromParts("package",
                                            BuildConfig.APPLICATION_ID, null)
                                    intent.data = uri
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                }
                            })
                            .show()
                }
            }
        }
    }

    private fun setButtonsState(show: Boolean) {
        btnStartDwnld?.isEnabled = !show
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (s == Constants.IS_DOWNLOADING) {
            setButtonsState(sharedPreferences.getBoolean(Constants.IS_DOWNLOADING,
                    false))
        }
    }

    companion object {

        var TAG = "MainActivity"

        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}
