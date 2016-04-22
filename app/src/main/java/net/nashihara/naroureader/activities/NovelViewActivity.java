package net.nashihara.naroureader.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ActivityNovelViewBinding;
import net.nashihara.naroureader.fragments.NovelBodyFragment;

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

    private String title = "";
    private int totalPage;
    private String ncode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_novel_view);
        manager = getSupportFragmentManager();

        Intent intent = getIntent();
        ncode = intent.getStringExtra("ncode");
        final int page = intent.getIntExtra("page", 1);

        totalPage = intent.getIntExtra("total", 0);
        title = intent.getStringExtra("title");

//        binding.toolbar.setTitle(title);
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.X);
        binding.toolbar.setNavigationIcon(materialMenu);
        binding.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Narou narou = new Narou();
                String body = narou.getNovelBody(ncode, page);
                subscriber.onNext(body);
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
                    }

                    @Override
                    public void onNext(String s) {
                        manager.beginTransaction()
                                .add(R.id.novel_container, NovelBodyFragment.newInstance(ncode, s, page, totalPage))
                                .commit();
                    }
                });
    }

    @Override
    public void onNovelBodyLoadAction(String body, int nextPage) {
        manager.beginTransaction()
                .replace(R.id.novel_container, NovelBodyFragment.newInstance(ncode, body, nextPage, totalPage))
                .commit();
    }
}
