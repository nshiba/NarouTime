package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.FragmentNovelBodyBinding;

import narou4j.Narou;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class NovelBodyFragment extends Fragment {
    private static final String TAG = NovelBodyFragment.class.getSimpleName();
    private static final String ARG_NCODE = "ncode";
    private static final String ARG_BODY = "body";
    private static final String ARG_PAGE = "page";
    private static final String ARG_TOTAL_PAGE = "total_page";

    private int page;
    private int totalPage;
    private String body;
    private String ncode;
    private String nextBody = "";
    private String prevBody = "";
    private String title = "";

    private Context mContext;
    private OnNovelBodyInteraction mListener;
    private FragmentNovelBodyBinding binding;

    public NovelBodyFragment() {}

    public static NovelBodyFragment newInstance(String ncode, String body, int page, int totalPage) {
        NovelBodyFragment fragment = new NovelBodyFragment();
        Bundle args = new Bundle();
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
        Bundle args = getArguments();
        if (args != null) {
            body = args.getString(ARG_BODY);
            ncode = args.getString(ARG_NCODE);
            totalPage = args.getInt(ARG_TOTAL_PAGE);
            page = args.getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_novel_body, container, false);
        binding.page.setText(String.valueOf(page) + "/" + String.valueOf(totalPage));
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page >= totalPage) {
                    return;
                }
                mListener.onNovelBodyLoadAction(nextBody, page+1);
            }
        });
        binding.btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page <= 1) {
                    return;
                }
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

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (body.equals("")) {
            Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    Narou narou = new Narou();
                    String str = narou.getNovelBody(ncode, page);
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
                            binding.body.setText(s);
                            binding.body.setVisibility(View.VISIBLE);
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    });
        }

        Log.d(TAG, "onActivityCreated: ncode -> " + ncode);
        Observable.combineLatest(Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Narou narou = new Narou();
                String str = narou.getNovelBody(ncode, page +1);
                subscriber.onNext(str);
            }
        }), Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Narou narou = new Narou();
                String str = narou.getNovelBody(ncode, page -1);
                subscriber.onNext(str);
            }
        }), new Func2<String, String, Pair<String, String>>() {
            @Override
            public Pair<String, String> call(String s, String s2) {
                return Pair.create(s, s2);
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Pair<String, String>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e.fillInStackTrace());
                    }

                    @Override
                    public void onNext(Pair<String, String> stringStringPair) {
                        nextBody = stringStringPair.first;
                        prevBody = stringStringPair.second;
                    }
                });
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
    }

    public interface OnNovelBodyInteraction {
        public void onNovelBodyLoadAction(String body, int nextPage);
    }
}