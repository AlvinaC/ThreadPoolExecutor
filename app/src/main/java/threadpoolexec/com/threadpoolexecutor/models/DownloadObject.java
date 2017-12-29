package threadpoolexec.com.threadpoolexecutor.models;

/**
 * Parcelable download object
 */

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadObject implements Parcelable {

    public DownloadObject() {

    }

    private int progress;
    private int currentFileSize;
    private int totalFileSize;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getCurrentFileSize() {
        return currentFileSize;
    }

    public void setCurrentFileSize(int currentFileSize) {
        this.currentFileSize = currentFileSize;
    }

    public int getTotalFileSize() {
        return totalFileSize;
    }

    public void setTotalFileSize(int totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(progress);
        dest.writeInt(currentFileSize);
        dest.writeInt(totalFileSize);
    }

    private DownloadObject(Parcel in) {

        progress = in.readInt();
        currentFileSize = in.readInt();
        totalFileSize = in.readInt();
    }

    public static final Parcelable.Creator<DownloadObject> CREATOR = new Parcelable.Creator<DownloadObject>() {
        public DownloadObject createFromParcel(Parcel in) {
            return new DownloadObject(in);
        }

        public DownloadObject[] newArray(int size) {
            return new DownloadObject[size];
        }
    };
}
