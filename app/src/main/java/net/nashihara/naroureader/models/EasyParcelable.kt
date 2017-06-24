package net.nashihara.naroureader.models

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.Gson

abstract class EasyParcelable : Parcelable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeString(gson.toJson(this))

    override fun toString(): String = javaClass.simpleName + gson.toJson(this)

    class EasyCreator<T : EasyParcelable>(internal val klass: Class<T>) : Parcelable.Creator<T> {

        override fun newArray(size: Int): Array<T> = uncheckedCast(arrayOfNulls<Any>(size))

        override fun createFromParcel(source: Parcel): T = readFromParcel(source, klass)
    }

    companion object {

        private val gson = Gson()

        fun <T : EasyParcelable> readFromParcel(source: Parcel, klass: Class<T>): T {
            val json = source.readString()
            return gson.fromJson(json, klass)
        }

        fun <R, V> uncheckedCast(value: V): R = value as R
    }
}
