package threadpoolexec.com.threadpoolexecutor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import threadpoolexec.com.threadpoolexecutor.services.DownloaderService;
import threadpoolexec.com.threadpoolexecutor.services.LocalBinder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = "MainActivity";
    private Button btn_start_dwnld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setListener();
    }

    private void setListener() {
        btn_start_dwnld.setOnClickListener(this);
    }

    private void init() {
        btn_start_dwnld = (Button) findViewById(R.id.btn_download);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting and binding service");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }

    public void doBindService(Intent i) {
        bindService(i, mConnection,
                Context.BIND_AUTO_CREATE);
    }

    public boolean mBounded;
    public DownloaderService mService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service bound");
            }
            mService = ((LocalBinder<DownloaderService>) service).getService();
            mBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service disconnect");
            }
            mBounded = false;
        }
    };

    @Override
    public void onClick(View view) {
        Intent i = new Intent(this, DownloaderService.class);
        startService(i);
        doBindService(i);
    }
}
