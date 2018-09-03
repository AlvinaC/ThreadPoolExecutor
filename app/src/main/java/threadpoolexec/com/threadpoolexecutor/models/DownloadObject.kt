package threadpoolexec.com.threadpoolexecutor.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadObject(var progress: Int = 0, var currentFileSize: Int = 0, var totalFileSize: Int = 0) : Parcelable