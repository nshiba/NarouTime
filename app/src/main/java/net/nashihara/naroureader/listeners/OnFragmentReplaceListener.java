package net.nashihara.naroureader.listeners;

import android.support.v4.app.Fragment;

import net.nashihara.naroureader.entities.NovelItem;

public interface OnFragmentReplaceListener {
    public void onFragmentReplaceAction(Fragment Fragment, String title, NovelItem item);
}
