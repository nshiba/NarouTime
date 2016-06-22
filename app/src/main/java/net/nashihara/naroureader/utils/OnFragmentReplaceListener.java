package net.nashihara.naroureader.utils;

import android.support.v4.app.Fragment;

import net.nashihara.naroureader.models.entities.NovelItem;

public interface OnFragmentReplaceListener {
    public void onFragmentReplaceAction(Fragment Fragment, String title, NovelItem item);
}
