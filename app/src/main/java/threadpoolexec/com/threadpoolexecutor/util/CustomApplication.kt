package threadpoolexec.com.threadpoolexecutor.util

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import threadpoolexec.com.threadpoolexecutor.DI.DaggerNetworkComponent
import threadpoolexec.com.threadpoolexecutor.DI.NetworkComponent
import threadpoolexec.com.threadpoolexecutor.DI.NetworksModule
import threadpoolexec.com.threadpoolexecutor.rxbus.RxBus

/**
 * Gives a dagger component
 * (in our case for a singleton Retrofit object across pool threads for download)
 */
class CustomApplication : Application() {

    var networkComponent: NetworkComponent? = null
        private set
    private var bus: RxBus? = null

    override fun onCreate() {
        super.onCreate()
        networkComponent = DaggerNetworkComponent.builder()
                //retrofit needs a baseurl...this url is not used...dynamic urls are used
                .networksModule(NetworksModule("http://google.com/"))
                .build()
        bus = RxBus()
    }

    fun bus(): RxBus? {
        return bus
    }

    companion object {

        fun requestingUpdates(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(Constants.IS_DOWNLOADING, false)
        }

        fun setRequestingUpdates(context: Context, requestingUpdates: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(Constants.IS_DOWNLOADING, requestingUpdates)
                    .apply()
        }
    }

}
