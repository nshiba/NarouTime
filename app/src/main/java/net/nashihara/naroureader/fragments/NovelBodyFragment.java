package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.FragmentNovelBodyBinding;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.entities.NovelBody4Realm;
import net.nashihara.naroureader.utils.RealmUtils;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import narou4j.Narou;
import narou4j.entities.NovelBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NovelBodyFragment extends Fragment implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final String TAG = NovelBodyFragment.class.getSimpleName();
    private static final String ARG_NCODE = "ncode";
    private static final String ARG_TITLE = "title";
    private static final String ARG_BODY = "body";
    private static final String ARG_PAGE = "page";
    private static final String ARG_TOTAL_PAGE = "total_page";

    private static final String PREF_IS_HIDE = "is_hide";

    private SharedPreferences pref;
    private Realm realm;
    private boolean isHide = false;

    private int page;
    private int totalPage;
    private String title;
    private String body;
    private String ncode;
    private String nextBody = "";
    private String prevBody = "";

    private Context mContext;
    private OnNovelBodyInteraction mListener;
    private FragmentNovelBodyBinding binding;
    private GestureDetector gestureDetector;

    public NovelBodyFragment() {}

    public static NovelBodyFragment newInstance(String ncode, String title, String body, int page, int totalPage) {
        NovelBodyFragment fragment = new NovelBodyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_BODY, body);
        args.putString(ARG_NCODE, ncode);
        args.putInt(ARG_PAGE, page);
        args.putInt(ARG_TOTAL_PAGE, totalPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        realm = RealmUtils.getRealm(mContext);
        gestureDetector = new GestureDetector(mContext, this);

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(ARG_TITLE);
            body = args.getString(ARG_BODY);
            ncode = args.getString(ARG_NCODE);
            totalPage = args.getInt(ARG_TOTAL_PAGE);
            page = args.getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_novel_body, container, false);

        binding.body.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        binding.page.setText(String.valueOf(page) + "/" + String.valueOf(totalPage));
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page >= totalPage) {
                    return;
                }
                realm.close();
                mListener.onNovelBodyLoadAction(nextBody, page+1, "");
            }
        });
        binding.btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page <= 1) {
                    return;
                }
                realm.close();
                mListener.onNovelBodyLoadAction(prevBody, page-1, "");
            }
        });

        if (body.equals("")) {
            goneBody();
        }
        else {
            binding.body.setText(body);
            binding.title.setText(title);
            visibleBody();
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        binding.scrollView.smoothScrollTo(binding.scrollView.getScrollX(), binding.scrollView.getScrollY() + 50);

        RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
        query.equalTo("ncode", ncode);
        RealmResults<Novel4Realm> results = query.findAll();

        if (body.equals("")) {
            if (results.size() == 0) {
                updateNovelBody(page);
            }
            else {
                RealmResults<NovelBody4Realm> targetBody = getNovelBody(ncode, page);

                if (targetBody.size() > 0) {
                    binding.body.setText(targetBody.get(0).getBody());
                    binding.title.setText(targetBody.get(0).getTitle());
                    visibleBody();
                }
                else {
                    updateNovelBody(page);
                }
            }
        }
        else {
            if (results.size() == 0) {
                if (pref.getBoolean(getString(R.string.auto_download), false)) {
                    addNovelBody(page, title, body);
                }
            } else {
                Novel4Realm novel4Realm = results.get(0);
                RealmResults<NovelBody4Realm> targetBody = getNovelBody(ncode, page);

                if (targetBody.size() > 0) {
                    if (pref.getBoolean(getString(R.string.auto_sync), false)) {
                        addNovelBody(page, title, body);
                    }
                }
                else {
                    if (pref.getBoolean(getString(R.string.auto_download), false)) {
                        addNovelBody(page, title, body);
                    }
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mListener = (OnNovelBodyInteraction) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        realm.close();
    }

    @Override
    public void onResume() {
        int text = pref.getInt(getString(R.string.body_text), 0);
        int background = pref.getInt(getString(R.string.body_background), 0);

        if (text != 0) {
            binding.page.setTextColor(text);
            binding.title.setTextColor(text);
            binding.body.setTextColor(text);
            binding.btnNext.setTextColor(text);
            binding.btnPrev.setTextColor(text);
        }

        if (background != 0) {
            binding.root.setBackgroundColor(background);
        }

        super.onResume();
    }

    private void addNovelBody(int page, String title, String body) {

        realm.beginTransaction();

        RealmResults<NovelBody4Realm> results = getNovelBody(ncode, page);
        if (results.size() > 0) {
            results.get(0).setNcode(ncode);
            results.get(0).setPage(page);
            results.get(0).setTitle(title);
            results.get(0).setBody(body);
        }
        else {
            NovelBody4Realm tmpBody = realm.createObject(NovelBody4Realm.class);
            tmpBody.setNcode(ncode);
            tmpBody.setPage(page);
            tmpBody.setTitle(title);
            tmpBody.setBody(body);
        }

        realm.commitTransaction();

    }

    private RealmResults<NovelBody4Realm> getNovelBody(String ncode, int page) {
        RealmResults<NovelBody4Realm> ncodeResults = realm.where(NovelBody4Realm.class).equalTo("ncode", ncode).findAll();
        return ncodeResults.where().equalTo("page", page).findAll();
    }

    private void updateNovelBody(final int targetPage) {
        Observable.create(new Observable.OnSubscribe<NovelBody>() {
            @Override
            public void call(Subscriber<? super NovelBody> subscriber) {
                Narou narou = new Narou();
                NovelBody novelBody = narou.getNovelBody(ncode, targetPage);
                subscriber.onNext(novelBody);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<NovelBody>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        onLoadError();
                        Log.e(TAG, "onError: ", e.fillInStackTrace());
                    }

                    @Override
                    public void onNext(NovelBody s) {
                        if (pref.getBoolean(getString(R.string.auto_download), false)) {
                            realm.beginTransaction();

                            RealmResults<NovelBody4Realm> results = getNovelBody(ncode, targetPage);
                            if (results.size() <= 0) {
                                NovelBody4Realm body4Realm = realm.createObject(NovelBody4Realm.class);
                                body4Realm.setNcode(ncode);
                                body4Realm.setTitle(s.getTitle());
                                body4Realm.setBody(s.getBody());
                                body4Realm.setPage(targetPage);
                            }
                            else {
                                NovelBody4Realm body4Realm = results.get(0);
                                body4Realm.setNcode(ncode);
                                body4Realm.setTitle(s.getTitle());
                                body4Realm.setBody(s.getBody());
                                body4Realm.setPage(targetPage);
                            }

                            realm.commitTransaction();
                        }
                        binding.body.setText(s.getBody());
                        binding.title.setText(s.getTitle());

                        visibleBody();
                    }
                });
    }

    private void visibleBody() {
        binding.body.setVisibility(View.VISIBLE);
        binding.title.setVisibility(View.VISIBLE);
        binding.btnPrev.setVisibility(View.VISIBLE);
        binding.page.setVisibility(View.VISIBLE);

        if (page == totalPage) {
            binding.btnNext.setVisibility(View.GONE);
            binding.readFinish.setVisibility(View.VISIBLE);
        }
        else {
            binding.btnNext.setVisibility(View.VISIBLE);
            binding.readFinish.setVisibility(View.GONE);
        }

        binding.progressBar.setVisibility(View.GONE);
    }

    private void goneBody() {
        binding.body.setVisibility(View.GONE);
        binding.title.setVisibility(View.GONE);
        binding.btnNext.setVisibility(View.GONE);
        binding.btnPrev.setVisibility(View.GONE);
        binding.page.setVisibility(View.GONE);
        binding.readFinish.setVisibility(View.GONE);

        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void reload() {
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    private void onLoadError() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnReload.setVisibility(View.VISIBLE);
        binding.btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnReload.setVisibility(View.GONE);
                reload();
            }
        });
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        isHide = pref.getBoolean(PREF_IS_HIDE, isHide);

        mListener.onSingleTapConfirmedAction(isHide);

        isHide = !isHide;
        pref.edit().putBoolean(PREF_IS_HIDE, isHide).apply();
        return false;
    }

    public interface OnNovelBodyInteraction {
        public void onNovelBodyLoadAction(String body, int nextPage, String bodyTitle);
        public Novel4Realm getNovel4RealmInstance();
        public void onSingleTapConfirmedAction(boolean isHide);
    }
}
