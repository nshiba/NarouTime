package net.nashihara.naroureader.entities

import android.os.Parcelable
import net.nashihara.naroureader.models.EasyParcelable

data class Query(
        var ncode: String = "",
        var limit: Int = 0,
        var sortOrder: Int = 0,
        var search: String = "",
        var notSearch: String = "",
        var isTargetTitle: Boolean = false,
        var isTargetStory: Boolean = false,
        var isTargetKeyword: Boolean = false,
        var isTargetWriter: Boolean = false,
        var time: Int = 0,
        var maxLength: Int = 0,
        var minLength: Int = 0,
        var isEnd: Boolean = false,
        var isStop: Boolean = false,
        var isPickup: Boolean = false) : EasyParcelable() {

    fun setLimitString(limitStr: String) {
        limit = validateLimit(limitStr)
    }

    fun setMaxLengthString(maxLengthString: String) {
        val max = if (maxLengthString == "") 0 else Integer.parseInt(maxLengthString)
        maxLength = max
    }

    fun setMinLengthString(minLengthString: String) {
        val min = if (minLengthString == "") 0 else Integer.parseInt(minLengthString)
        minLength = min
    }

    private fun validateLimit(limitStr: String): Int {
        return if (limitStr == "") 0 else Integer.parseInt(limitStr)
    }

    companion object {

        val CREATOR: Parcelable.Creator<Query> = EasyParcelable.EasyCreator(Query::class.java)
    }
}
