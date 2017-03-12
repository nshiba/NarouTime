package net.nashihara.naroureader.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

public abstract class EasyParcelable implements Parcelable {

    private static Gson gson = new Gson();

    public static <T extends EasyParcelable> T readFromParcel(@NonNull Parcel source, @NonNull Class<T> klass) {
        String json = source.readString();
        return gson.fromJson(json, klass);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(gson.toJson(this));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + gson.toJson(this);
    }

    public static class EasyCreator<T extends EasyParcelable> implements Creator<T> {

        final Class<T> klass;

        public EasyCreator(Class<T> klass) {
            this.klass = klass;
        }

        @Override
        public T createFromParcel(Parcel source) {
            return readFromParcel(source, klass);
        }

        @Override
        public T[] newArray(int size) {
            return uncheckedCast(new Object[size]);
        }
    }

    @SuppressWarnings("unchecked")
    public static <R, V> R uncheckedCast(V value) {
        return (R) value;
    }
}
