package threadpoolexec.com.threadpoolexecutor.services

/**
 * Custom Binder class
 */

import android.os.Binder

import java.lang.ref.WeakReference

class LocalBinder<S>(service: S) : Binder() {
    private val mService: WeakReference<S> = WeakReference(service)

    val service: S?
        get() = mService.get()

}