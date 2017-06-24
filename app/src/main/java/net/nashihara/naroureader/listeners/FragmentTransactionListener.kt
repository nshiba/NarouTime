package net.nashihara.naroureader.listeners

import android.support.v4.app.Fragment

import net.nashihara.naroureader.entities.NovelItem

interface FragmentTransactionListener {
    fun replaceFragment(Fragment: Fragment, title: String, item: NovelItem)
}
