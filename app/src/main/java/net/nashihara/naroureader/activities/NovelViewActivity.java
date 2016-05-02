package net.nashihara.naroureader.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.balysv.materialmenu.MaterialMenuDrawable;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.RealmUtils;
import net.nashihara.naroureader.databinding.ActivityNovelViewBinding;
import net.nashihara.naroureader.dialogs.OkCancelDialogFragment;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.fragments.NovelBodyFragment;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import narou4j.Narou;
import narou4j.entities.NovelBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NovelViewActivity extends AppCompatActivity implements NovelBodyFragment.OnNovelBodyInteraction {
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
        Observable.create(new Observable.OnSubscribe<NovelBody>() {
            @Override
            public void call(Subscriber<? super NovelBody> subscriber) {
                Narou narou = new Narou();
                try {
                    NovelBody body = narou.getNovelBody(ncode, page);
                    subscriber.onNext(body);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<NovelBody>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e.fillInStackTrace());
                        onLoadError();
                    }

                    @Override
                    public void onNext(NovelBody novelBody) {
                        manager.beginTransaction()
                                .add(R.id.novel_container, NovelBodyFragment.newInstance(ncode, novelBody.getTitle(), novelBody.getBody(), novelBody.getPage(), totalPage))
                                .commit();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        pref.edit().putBoolean(PREF_IS_HIDE, false).apply();

        boolean autoRemoveBookmark = pref.getBoolean(getString(R.string.auto_bookmark), false);
        if (autoRemoveBookmark) {

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
                realm.beginTransaction();

                Novel4Realm bookmarkNovel = getNovel4RealmInstance();
                bookmarkNovel.setBookmark(nowPage);

                realm.commitTransaction();
            }
        }

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

        realm.beginTransaction();

        Novel4Realm novel4Realm = realm.createObject(Novel4Realm.class);
        novel4Realm.setTitle(title);
        novel4Realm.setWriter(writer);
        novel4Realm.setNcode(ncode);
        novel4Realm.setTotalPage(totalPage);

        realm.commitTransaction();
        return novel4Realm;
    }

    @Override
    public void onSingleTapConfirmedAction(boolean isHide) {
        View decor = this.getWindow().getDecorView();
        if (isHide) {
            if (toolBarHeight > 0) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.novelContainer.getLayoutParams();
                params.topMargin = toolBarHeight;
                binding.novelContainer.setLayoutParams(params);
            }

            binding.appBar.setVisibility(View.VISIBLE);
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        else {
            toolBarHeight = binding.appBar.getHeight();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.novelContainer.getLayoutParams();
            params.topMargin = 0;
            binding.novelContainer.setLayoutParams(params);

            binding.appBar.setVisibility(View.GONE);
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    private void onLoadError() {
        OkCancelDialogFragment dialogFragment =
                new OkCancelDialogFragment("読み込みに失敗しました。", "再読み込みしますか？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == OkCancelDialogFragment.OK) {
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }

                        if (which == OkCancelDialogFragment.CANSEL) {
                            finish();
                        }
                    }
                });

        dialogFragment.show(getSupportFragmentManager(), "okcansel");
    }
}