package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.content.DialogInterface;
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
import net.nashihara.naroureader.RealmUtils;
import net.nashihara.naroureader.databinding.FragmentNovelBodyBinding;
import net.nashihara.naroureader.dialogs.OkCancelDialogFragment;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.entities.NovelBody4Realm;

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

        boolean autoRemoveBookmark = pref.getBoolean(getString(R.string.auto_remove_bookmark), false);
        if (autoRemoveBookmark) {
            RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
            query.equalTo("ncode", ncode);
            RealmResults<Novel4Realm> results = query.findAll();

            if (results.size() != 0) {
                realm.beginTransaction();

                Novel4Realm novel4Realm = results.get(0);
                int bookmarkPage = novel4Realm.getBookmark();

                if (bookmarkPage == page) {
                    novel4Realm.setBookmark(0);
                    realm.commitTransaction();
                }
                else {
                    realm.cancelTransaction();
                }
            }
        }

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

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = (String) binding.title.getText();
                StringBuilder builder = new StringBuilder();
                builder.append(title);
                builder.append("にしおりをはさみますか？");
                OkCancelDialogFragment dialogFragment
                        = new OkCancelDialogFragment("Bookmark", builder.toString(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == OkCancelDialogFragment.OK) {
                            bookmark();
                        }
                    }
                });
                dialogFragment.show(getFragmentManager(), "okcansel");
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        isHide = pref.getBoolean(PREF_IS_HIDE, isHide);
        if (isHide) {
            binding.fab.setVisibility(View.GONE);
        }

        RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
        query.equalTo("ncode", ncode);
        RealmResults<Novel4Realm> results = query.findAll();

        if (body.equals("")) {
            Log.d(TAG, "onActivityCreated: body equals \"\"");
            if (results.size() == 0) {
                Log.d(TAG, "onActivityCreated: results.size == 0");
                Novel4Realm tmpNovel = mListener.getNovel4RealmInstance();
                updateNovelBody(page);
            }
            else {
                Log.d(TAG, "onActivityCreated: results.size != 0");
                Novel4Realm novel4Realm = results.get(0);
                RealmResults<NovelBody4Realm> targetBody = getNovelBody(ncode, page);

                if (targetBody.size() > 0) {
                    Log.d(TAG, "onActivityCreated: getNovelBody true");
                    binding.body.setText(targetBody.get(0).getBody());
                    binding.title.setText(targetBody.get(0).getTitle());
                    visibleBody();
                }
                else {
                    Log.d(TAG, "onActivityCreated: getNovelBody false");
                    updateNovelBody(page);
                }
            }
        }
        else {
            Log.d(TAG, "onActivityCreated: body not equals \"\"");
            if (results.size() == 0) {
                Log.d(TAG, "onActivityCreated: results.size == 0");
                if (pref.getBoolean(getString(R.string.auto_download), false)) {
                    addNovelBody(page, title, body);
                }
            } else {
                Log.d(TAG, "onActivityCreated: results.size != 0");
                Novel4Realm novel4Realm = results.get(0);
                RealmResults<NovelBody4Realm> targetBody = getNovelBody(ncode, page);

                if (targetBody.size() > 0) {
                    Log.d(TAG, "onActivityCreated: getNovelBody true");
                    if (pref.getBoolean(getString(R.string.auto_sync), false)) {
                        addNovelBody(page, title, body);
                    }
                }
                else {
                    Log.d(TAG, "onActivityCreated: getNovelBody false");
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

    private RealmResults<Novel4Realm> getRealmResult() {
        RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
        query.equalTo("ncode", ncode);
        RealmResults<Novel4Realm> results = query.findAll();
        return results;
    }

    public void bookmark() {
        RealmResults<Novel4Realm> results = getRealmResult();

        if (results.size() != 0) {
            realm.beginTransaction();

            Novel4Realm novel4Realm = results.get(0);
            novel4Realm.setBookmark(page);
            novel4Realm.setTotalPage(totalPage);

            realm.commitTransaction();
        }
        else {
            Novel4Realm novel4Realm = mListener.getNovel4RealmInstance();
            realm.beginTransaction();
            novel4Realm.setBookmark(page);
            realm.commitTransaction();
        }
    }

    private RealmResults<NovelBody4Realm> getNovelBody(String ncode, int page) {
        Log.d(TAG, "getNovelBody: ncode -> " + ncode);
        Log.d(TAG, "getNovelBody: page -> " + page);
        RealmResults<NovelBody4Realm> ncodeResults = realm.where(NovelBody4Realm.class).equalTo("ncode", ncode).findAll();
        return ncodeResults.where().equalTo("page", page).findAll();
    }

    private void updateNovelBody(final int targetPage) {
        Log.d(TAG, "updateNovelBody: ");
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
        binding.btnNext.setVisibility(View.VISIBLE);
        binding.btnPrev.setVisibility(View.VISIBLE);
        binding.page.setVisibility(View.VISIBLE);

        binding.progressBar.setVisibility(View.GONE);
    }

    private void goneBody() {
        binding.body.setVisibility(View.GONE);
        binding.title.setVisibility(View.GONE);
        binding.btnNext.setVisibility(View.GONE);
        binding.btnPrev.setVisibility(View.GONE);
        binding.page.setVisibility(View.GONE);

        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG, "onSingleTapUp: ");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "onLongPress: ");
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onScroll: ");
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "onFling: ");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(TAG, "onShowPress: ");
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d(TAG, "onDoubleTap: ");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.d(TAG, "onDoubleTapEvent: ");
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG, "onSingleTapConfirmed: ");
        isHide = pref.getBoolean(PREF_IS_HIDE, isHide);

        mListener.onSingleTapConfirmedAction(isHide);

        if (isHide) {
            binding.fab.setVisibility(View.VISIBLE);
        }
        else {
            binding.fab.setVisibility(View.GONE);
        }

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
