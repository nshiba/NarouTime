package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.controller.NovelBodyController;
import net.nashihara.naroureader.databinding.FragmentNovelBodyBinding;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.utils.RealmUtils;
import net.nashihara.naroureader.views.NovelBodyView;

import io.realm.Realm;
import narou4j.entities.NovelBody;

public class NovelBodyFragment extends Fragment implements NovelBodyView {

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

    private Context context;

    private OnNovelBodyInteraction listener;

    private FragmentNovelBodyBinding binding;

    private NovelBodyController controller;

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
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        realm = RealmUtils.getRealm(context);
        controller = new NovelBodyController(this, realm);

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

        if (body.equals("")) {
            goneBody();
        } else {
            binding.body.setText(body);
            binding.title.setText(title);
            visibleBody();
        }

        setupPageButton();

        return binding.getRoot();
    }

    private void setupPageButton() {
        binding.page.setText(String.valueOf(page) + "/" + String.valueOf(totalPage));
        binding.btnNext.setOnClickListener(v -> {
            if (page >= totalPage) {
                return;
            }

            realm.close();
            listener.onNovelBodyLoadAction(nextBody, page + 1, "");
        });

        binding.btnPrev.setOnClickListener(v -> {
            if (page <= 1) {
                return;
            }

            realm.close();
            listener.onNovelBodyLoadAction(prevBody, page - 1, "");
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        boolean autoDownload = pref.getBoolean(getString(R.string.auto_download), false);
        boolean autoSync = pref.getBoolean(getString(R.string.auto_sync), false);
        controller.setupNovelPage(ncode, title, body, page, autoDownload, autoSync);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        listener = (OnNovelBodyInteraction) context;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        realm = RealmUtils.getRealm(context);
        controller = new NovelBodyController(this, realm);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        realm.close();
        controller.detach();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupPageColor();
    }

    private void setupPageColor() {
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
        binding.btnReload.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnReload.setVisibility(View.GONE);
            reload();
        });
    }

    @Override
    public void showNovelBody(NovelBody novelBody) {
        binding.body.setText(novelBody.getBody());
        binding.title.setText(novelBody.getTitle());
        visibleBody();
    }

    @Override
    public void showError() {
        onLoadError();
    }

    public interface OnNovelBodyInteraction {
        void onNovelBodyLoadAction(String body, int nextPage, String bodyTitle);
        Novel4Realm getNovel4RealmInstance();
    }
}
