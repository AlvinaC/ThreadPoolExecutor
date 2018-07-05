package threadpoolexec.com.threadpoolexecutor.util;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import threadpoolexec.com.threadpoolexecutor.DI.DaggerNetworkComponent;
import threadpoolexec.com.threadpoolexecutor.DI.NetworkComponent;
import threadpoolexec.com.threadpoolexecutor.DI.NetworksModule;
import threadpoolexec.com.threadpoolexecutor.rxbus.RxBus;

/**
 * Gives a dagger component
 * (in our case for a singleton Retrofit object across pool threads for download)
 */
public class CustomApplication extends Application {

    private NetworkComponent networkComponent;
    private RxBus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        networkComponent = DaggerNetworkComponent.builder()
                //retrofit needs a baseurl...this url is not used...dynamic urls are used
                .networksModule(new NetworksModule("http://google.com/"))
                .build();
        bus = new RxBus();
    }

    public NetworkComponent getNetworkComponent() {
        return networkComponent;
    }

    public RxBus bus() {
        return bus;
    }

    public static boolean requestingUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Constants.IS_DOWNLOADING, false);
    }

    public static void setRequestingUpdates(Context context, boolean requestingUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Constants.IS_DOWNLOADING, requestingUpdates)
                .apply();
    }

}
