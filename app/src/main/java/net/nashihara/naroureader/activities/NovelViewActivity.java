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

import com.balysv.materialmenu.MaterialMenuDrawable;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ActivityNovelViewBinding;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.fragments.NovelBodyFragment;
import net.nashihara.naroureader.fragments.OkCancelDialogFragment;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import narou4j.Narou;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NovelViewActivity extends AppCompatActivity implements NovelBodyFragment.OnNovelBodyInteraction {
    private final static String TAG = NovelViewActivity.class.getSimpleName();
    ActivityNovelViewBinding binding;
    private FragmentManager manager;
    private MaterialMenuDrawable materialMenu;

    private ArrayList<String> bodyTitles;
    private String title;
    private int totalPage;
    private String ncode;
    private String writer;

    private int nowPage;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_novel_view);
        manager = getSupportFragmentManager();
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        ncode = intent.getStringExtra("ncode");
        final int page = intent.getIntExtra("page", 1);
        nowPage = page;

        title = intent.getStringExtra("title");
        writer = intent.getStringExtra("writer");
        bodyTitles = intent.getStringArrayListExtra("titles");
        totalPage = bodyTitles.size();

        binding.toolbar.setTitle(bodyTitles.get(page -1));
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.X);
        binding.toolbar.setNavigationIcon(materialMenu);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Narou narou = new Narou();
                try {
                    String body = narou.getNovelBody(ncode, page);
                    subscriber.onNext(body);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e.fillInStackTrace());
                        onLoadError();
                    }

                    @Override
                    public void onNext(String s) {
                        manager.beginTransaction()
                                .add(R.id.novel_container, NovelBodyFragment.newInstance(ncode, bodyTitles.get(page -1), s, page, totalPage))
                                .commit();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        boolean autoRemoveBookmark = pref.getBoolean(getString(R.string.auto_bookmark), false);
        if (autoRemoveBookmark) {
            RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplicationContext()).build();
            Realm.setDefaultConfiguration(realmConfig);
            Realm realm = Realm.getDefaultInstance();

            ncode = ncode.toLowerCase();
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

                Novel4Realm bookmarkNovel = getNovel4RealmInstance(realm);
                bookmarkNovel.setBookmark(nowPage);

                realm.commitTransaction();
            }
        }

        finish();

        super.onBackPressed();
    }

    @Override
    public void onNovelBodyLoadAction(String body, int nextPage) {
        nowPage = nextPage -1;
        binding.toolbar.setTitle(bodyTitles.get(nextPage -1));
        manager.beginTransaction()
                .replace(R.id.novel_container, NovelBodyFragment.newInstance(ncode, bodyTitles.get(nextPage -1), body, nextPage, totalPage))
                .commit();
    }

    @Override
    public Novel4Realm getNovel4RealmInstance(Realm realm) {
        ncode = ncode.toLowerCase();

        realm.beginTransaction();

        Novel4Realm novel4Realm = realm.createObject(Novel4Realm.class);
        novel4Realm.setTitle(title);
        novel4Realm.setWriter(writer);
        novel4Realm.setNcode(ncode);

        realm.commitTransaction();
        return novel4Realm;
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