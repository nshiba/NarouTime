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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.RealmUtils;
import net.nashihara.naroureader.databinding.FragmentNovelBodyBinding;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.entities.NovelBody4Realm;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import narou4j.Narou;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NovelBodyFragment extends Fragment {
    private static final String TAG = NovelBodyFragment.class.getSimpleName();
    private static final String ARG_NCODE = "ncode";
    private static final String ARG_TITLE = "title";
    private static final String ARG_BODY = "body";
    private static final String ARG_PAGE = "page";
    private static final String ARG_TOTAL_PAGE = "total_page";

    private SharedPreferences pref;
    private Realm realm;

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
        realm = RealmUtils.getRealm(mContext, 0);

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

        boolean autoRemoveBookmark = pref.getBoolean(getString(R.string.auto_remove_bookmark), false);
        if (autoRemoveBookmark) {
            ncode = ncode.toLowerCase();
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
                mListener.onNovelBodyLoadAction(nextBody, page+1);
            }
        });
        binding.btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page <= 1) {
                    return;
                }
                realm.close();
                mListener.onNovelBodyLoadAction(prevBody, page-1);
            }
        });

        if (body.equals("")) {
            binding.body.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.body.setText(body);
            binding.body.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }

        binding.title.setText(title);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder builder = new StringBuilder();
                builder.append(title);
                builder.append("にしおりをはさみますか？");
                OkCancelDialogFragment dialogFragment
                        = new OkCancelDialogFragment("Bookmark", builder.toString(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == OkCancelDialogFragment.OK) {
                            realm.close();
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

        ncode = ncode.toLowerCase();
        RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
        query.equalTo("ncode", ncode);
        RealmResults<Novel4Realm> results = query.findAll();

        if (body.equals("")) {
            Log.d(TAG, "onActivityCreated: body equals \"\"");
            if (results.size() == 0) {
                Log.d(TAG, "onActivityCreated: results.size == 0");
                Novel4Realm tmpNovel = mListener.getNovel4RealmInstance(realm);
                updateNovelBody(page, tmpNovel);
            }
            else {
                Log.d(TAG, "onActivityCreated: results.size != 0");
                Novel4Realm novel4Realm = results.get(0);
                RealmResults<NovelBody4Realm> targetBody = getNovelBody(ncode, page);

                if (targetBody.size() > 0) {
                    Log.d(TAG, "onActivityCreated: getNovelBody true");
                    binding.body.setText(targetBody.get(0).getBody());
                    binding.body.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                }
                else {
                    Log.d(TAG, "onActivityCreated: getNovelBody false");
                    updateNovelBody(page, novel4Realm);
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

//            Log.d(TAG, "onActivityCreated: ncode -> " + ncode);
//            Observable.combineLatest(Observable.create(new Observable.OnSubscribe<String>() {
//                        @Override
//                        public void call(Subscriber<? super String> subscriber) {
//                            getNextPage4Rx(page + 1, subscriber);
//                        }
//                    })
//                    , Observable.create(new Observable.OnSubscribe<String>() {
//                        @Override
//                        public void call(Subscriber<? super String> subscriber) {
//                            getNextPage4Rx(page - 1, subscriber);
//                        }
//                    })
//                    , new Func2<String, String, Pair<String, String>>() {
//                        @Override
//                        public Pair<String, String> call(String s, String s2) {
//                            return Pair.create(s, s2);
//                        }
//                    })
//                    .subscribe(new Subscriber<Pair<String, String>>() {
//                        @Override
//                        public void onCompleted() {
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            Log.e(TAG, "onError: ", e.fillInStackTrace());
//                        }
//
//                        @Override
//                        public void onNext(Pair<String, String> stringStringPair) {
//                            nextBody = stringStringPair.first;
//                            prevBody = stringStringPair.second;
//                        }
//                    });
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

//    private void getNextPage4Rx(int targetPage, Subscriber<? super String> subscriber) {
//        RealmResults<Novel4Realm> results = getRealmResult();
//        if (results.size() > 0) {
//            Novel4Realm novel4Realm = results.get(0);
//            RealmResults<NovelBody4Realm> resultBodies = novel4Realm.getBodies().where().equalTo("page", page).findAll();
//            if (resultBodies.size() > 0) {
//                String body = resultBodies.get(0).getBody();
//                subscriber.onNext(body);
//                return;
//            }
//        }
//
//        Narou narou = new Narou();
//        String str = narou.getNovelBody(ncode, targetPage);
//        subscriber.onNext(str);
//    }
    
    private RealmResults<Novel4Realm> getRealmResult() {
        ncode = ncode.toLowerCase();
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

            realm.commitTransaction();
        }
        else {
            Novel4Realm novel4Realm = mListener.getNovel4RealmInstance(realm);
            realm.beginTransaction();
            novel4Realm.setBookmark(page);
            realm.commitTransaction();
        }
    }

    private RealmResults<NovelBody4Realm> getNovelBody(String ncode, int page) {
        RealmResults<NovelBody4Realm> ncodeResults = realm.where(NovelBody4Realm.class).equalTo("ncode", ncode).findAll();
        return ncodeResults.where().equalTo("page", page).findAll();
    }

    private void updateNovelBody(final int targetPage, final Novel4Realm novel4Realm) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Narou narou = new Narou();
                String str = narou.getNovelBody(ncode, targetPage);
                subscriber.onNext(str);
                subscriber.onCompleted();
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
                        if (pref.getBoolean(getString(R.string.auto_download), false)) {
                            realm.beginTransaction();

                            RealmResults<NovelBody4Realm> results = getNovelBody(ncode, targetPage);
                            if (results.size() <= 0) {
                                NovelBody4Realm body4Realm = realm.createObject(NovelBody4Realm.class);
                                body4Realm.setNcode(ncode);
                                body4Realm.setTitle(title);
                                body4Realm.setBody(s);
                                body4Realm.setPage(targetPage);
                            }
                            else {
                                NovelBody4Realm body4Realm = results.get(0);
                                body4Realm.setNcode(ncode);
                                body4Realm.setTitle(title);
                                body4Realm.setBody(s);
                                body4Realm.setPage(targetPage);
                            }

                            realm.commitTransaction();
                        }
                        binding.body.setText(s);
                        binding.body.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public interface OnNovelBodyInteraction {
        public void onNovelBodyLoadAction(String body, int nextPage);
        public Novel4Realm getNovel4RealmInstance(Realm realm);
    }
}
