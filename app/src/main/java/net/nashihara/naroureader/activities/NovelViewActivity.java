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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.balysv.materialmenu.MaterialMenuDrawable;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ActivityNovelViewBinding;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.fragments.NovelBodyFragment;
import net.nashihara.naroureader.utils.RealmUtils;

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

    private int toolBarHeight = 0;
    private int nowPage;
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
        nowPage = page;

        title = intent.getStringExtra("title");
        writer = intent.getStringExtra("writer");
        bodyTitle = intent.getStringExtra("bodyTitle");
        totalPage = intent.getIntExtra("totalPage", 0);

        binding.toolbar.setTitle(bodyTitle);
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.X);
        binding.toolbar.setTitle(title);
        binding.toolbar.setNavigationIcon(materialMenu);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.toolbar.inflateMenu(R.menu.menu_novelbody);
        binding.toolbar.setOnMenuItemClickListener(this);
        onNovelBodyLoadAction("", nowPage, bodyTitle);
    }

    @Override
    public void onBackPressed() {
        pref.edit().putBoolean(PREF_IS_HIDE, false).apply();

        boolean autoRemoveBookmark = pref.getBoolean(getString(R.string.auto_bookmark), false);
        if (autoRemoveBookmark) {

            realm = RealmUtils.getRealm(this);
            RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
            query.equalTo("ncode", ncode);
            RealmResults<Novel4Realm> results = query.findAll();

            if (results.size() != 0) {
                realm.beginTransaction();

                Novel4Realm novel4Realm = results.get(0);
                novel4Realm.setBookmark(nowPage);

                realm.commitTransaction();
            }
            else {
                Novel4Realm bookmarkNovel = getNovel4RealmInstance();

                realm.beginTransaction();
                bookmarkNovel.setBookmark(nowPage);

                realm.commitTransaction();
            }
        }
        realm.close();

        finish();

        super.onBackPressed();
    }

    @Override
    public void onNovelBodyLoadAction(String body, int nextPage, String bodyTitle) {
        nowPage = nextPage -1;
        manager.beginTransaction()
                .replace(R.id.novel_container, NovelBodyFragment.newInstance(ncode, bodyTitle, body, nextPage, totalPage))
                .commit();
    }

    @Override
    public Novel4Realm getNovel4RealmInstance() {

        realm = RealmUtils.getRealm(this);
        realm.beginTransaction();

        Novel4Realm novel4Realm = realm.createObject(Novel4Realm.class);
        novel4Realm.setTitle(title);
        novel4Realm.setWriter(writer);
        novel4Realm.setNcode(ncode);
        novel4Realm.setTotalPage(totalPage);

        realm.commitTransaction();
        realm.close();
        return novel4Realm;
    }

    @Override
    public void onSingleTapConfirmedAction(boolean isHide) {
        if (isHide) {
            if (toolBarHeight > 0) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.novelContainer.getLayoutParams();
                params.topMargin = toolBarHeight;
                binding.novelContainer.setLayoutParams(params);
            }

            binding.appBar.setVisibility(View.VISIBLE);
        }
        else {
            toolBarHeight = binding.appBar.getHeight();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.novelContainer.getLayoutParams();
            params.topMargin = 0;
            binding.novelContainer.setLayoutParams(params);

            binding.appBar.setVisibility(View.GONE);
        }
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
}