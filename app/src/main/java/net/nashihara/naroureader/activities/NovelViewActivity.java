package net.nashihara.naroureader.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.balysv.materialmenu.MaterialMenuDrawable;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ActivityNovelViewBinding;
import net.nashihara.naroureader.fragments.NovelBodyFragment;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.utils.RealmUtils;
import net.nashihara.naroureader.adapters.NovelBodyFragmentViewPagerAdapter;
import net.nashihara.naroureader.widgets.OkCancelDialogFragment;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class NovelViewActivity extends AppCompatActivity implements NovelBodyFragment.OnNovelBodyInteraction, Toolbar.OnMenuItemClickListener {


    private final static String TAG = NovelViewActivity.class.getSimpleName();
    ActivityNovelViewBinding binding;
    private FragmentManager manager;
    private MaterialMenuDrawable materialMenu;
    private Realm realm;

    private String bodyTitle;
    private String title;
    private int totalPage;
    private String ncode;
    private String writer;

    private SharedPreferences pref;
    private static final String PREF_IS_HIDE = "is_hide";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_novel_view);
        manager = getSupportFragmentManager();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        realm = RealmUtils.getRealm(this);

        Intent intent = getIntent();
        ncode = intent.getStringExtra("ncode");
        final int page = intent.getIntExtra("page", 1);

        title = intent.getStringExtra("title");
        writer = intent.getStringExtra("writer");
        bodyTitle = intent.getStringExtra("bodyTitle");
        totalPage = intent.getIntExtra("totalPage", 0);

        binding.toolbar.setTitle(bodyTitle);
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.X);
        binding.toolbar.setTitle(title);
        binding.toolbar.setNavigationIcon(materialMenu);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.toolbar.inflateMenu(R.menu.menu_novelbody);
        binding.toolbar.setOnMenuItemClickListener(this);

        NovelBodyFragmentViewPagerAdapter adapter
            = new NovelBodyFragmentViewPagerAdapter(getSupportFragmentManager(), ncode, title, totalPage);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(page -1);

        binding.fab.setOnClickListener(v -> {
            title = (String) binding.toolbar.getTitle();
            StringBuilder builder = new StringBuilder();
            builder.append(title);
            builder.append("にしおりをはさみますか？");
            OkCancelDialogFragment dialogFragment
                = OkCancelDialogFragment.newInstance("しおり", builder.toString(), (dialog, which) -> {
                if (which == OkCancelDialogFragment.OK) {
                    bookmark(binding.viewPager.getCurrentItem() +1);
                }
            });
            dialogFragment.show(getSupportFragmentManager(), "okcansel");
        });

        boolean autoRemoveBookmark = pref.getBoolean(getString(R.string.auto_remove_bookmark), false);
        if (autoRemoveBookmark) {
            removeBookmark();
        }
    }

    @Override
    public void onBackPressed() {
        pref.edit().putBoolean(PREF_IS_HIDE, false).apply();

        boolean autoBookmark = pref.getBoolean(getString(R.string.auto_bookmark), false);
        Log.d(TAG, "onBackPressed: auto bookmark" + autoBookmark);
        if (autoBookmark) {
            bookmark(binding.viewPager.getCurrentItem() +1);
        }

        finish();

        super.onBackPressed();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "dispatchKeyEvent: ");

        int volume_type = Integer.parseInt(pref.getString(getString(R.string.hardware_btn_volume), "0"));

        if (volume_type == 0) {
            return super.dispatchKeyEvent(event);
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP ||
            event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {

            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return super.dispatchKeyEvent(event);
            }

            int diff;
            if (volume_type == 1) {
                diff = 1;
            }
            else {
                diff = -1;
            }

            int nowPage = binding.viewPager.getCurrentItem();
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                binding.viewPager.setCurrentItem(nowPage + diff);
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                binding.viewPager.setCurrentItem(nowPage - diff);
            }
            return true;
        }
        else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onNovelBodyLoadAction(String body, int nextPage, String bodyTitle) {
        Log.d(TAG, "onNovelBodyLoadAction: " + nextPage);
        binding.viewPager.setCurrentItem(nextPage -1);
    }

    @Override
    public Novel4Realm getNovel4RealmInstance() {
        if (realm.isClosed()) {
            realm = RealmUtils.getRealm(this);
        }

        Novel4Realm novel4Realm = new Novel4Realm();
        novel4Realm.setTitle(title);
        novel4Realm.setWriter(writer);
        novel4Realm.setNcode(ncode);
        novel4Realm.setTotalPage(totalPage);
        Log.d(TAG, "getNovel4RealmInstance: " + novel4Realm.toString());
        realm.executeTransaction(realmTransaction -> realmTransaction.copyToRealmOrUpdate(novel4Realm));
        return novel4Realm;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.body_setting: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
        }

        return true;
    }

    private RealmResults<Novel4Realm> getRealmResult() {
        if (realm.isClosed()) {
            realm = RealmUtils.getRealm(this);
        }

        RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
        query.equalTo("ncode", ncode);
        RealmResults<Novel4Realm> results = query.findAll();
        return results;
    }

    public void bookmark(int page) {
        Novel4Realm novel4Realm = new Novel4Realm();
        novel4Realm.setTitle(title);
        novel4Realm.setWriter(writer);
        novel4Realm.setNcode(ncode);
        novel4Realm.setTotalPage(totalPage);
        novel4Realm.setBookmark(page);
        Log.d(TAG, "getNovel4RealmInstance: " + novel4Realm.toString());
        realm.executeTransaction(realmTransaction -> realmTransaction.copyToRealmOrUpdate(novel4Realm));
//        RealmResults<Novel4Realm> results = getRealmResult();
//
//        if (results.size() != 0) {
//            realm.beginTransaction();
//
//            Novel4Realm novel4Realm = results.get(0);
//            novel4Realm.setBookmark(page);
//            novel4Realm.setTotalPage(totalPage);
//
//            realm.commitTransaction();
//        }
//        else {
//            Novel4Realm novel4Realm = getNovel4RealmInstance();
//            realm.beginTransaction();
//            novel4Realm.setBookmark(page);
//            realm.commitTransaction();
//        }
    }

    public void removeBookmark() {
        RealmResults<Novel4Realm> results = getRealmResult();

        if (results.size() != 0) {
            realm.beginTransaction();

            Novel4Realm novel4Realm = results.get(0);
            int bookmarkPage = novel4Realm.getBookmark();

            if (bookmarkPage > 0) {
                novel4Realm.setBookmark(0);
                realm.commitTransaction();
            }
            else {
                realm.cancelTransaction();
            }
        }
    }
}