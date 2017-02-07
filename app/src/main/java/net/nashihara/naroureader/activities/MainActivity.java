package net.nashihara.naroureader.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ActivityMainBinding;
import net.nashihara.naroureader.fragments.BookmarkRecyclerViewFragment;
import net.nashihara.naroureader.fragments.DownloadedRecyclerViewFragment;
import net.nashihara.naroureader.fragments.NovelTableRecyclerViewFragment;
import net.nashihara.naroureader.fragments.RankingViewPagerFragment;
import net.nashihara.naroureader.fragments.SearchFragment;
import net.nashihara.naroureader.fragments.SearchRecyclerViewFragment;
import net.nashihara.naroureader.listeners.FragmentTransactionListener;
import net.nashihara.naroureader.entities.NovelItem;
import net.nashihara.naroureader.utils.DownloadUtils;
import net.nashihara.naroureader.utils.NetworkUtils;
import net.nashihara.naroureader.widgets.ListDailogFragment;
import net.nashihara.naroureader.widgets.NovelDownloadDialogFragment;
import net.nashihara.naroureader.widgets.OkCancelDialogFragment;

import java.util.Stack;

import narou4j.entities.Novel;
import narou4j.enums.RankingType;

import static android.support.v4.view.GravityCompat.START;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, FragmentTransactionListener,
    NovelTableRecyclerViewFragment.OnNovelSelectionListener, Toolbar.OnMenuItemClickListener {

    private ActivityMainBinding binding;

    private String TAG = MainActivity.class.getSimpleName();

    private FragmentManager fragmentManager;

    private Stack<CharSequence> titleStack = new Stack<>();

    private MaterialMenuDrawable materialMenu;

    private boolean isNovelTableView = false;

    private NovelItem downloadTargetNovel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
        binding.toolbar.setNavigationIcon(materialMenu);
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (materialMenu.getIconState() == MaterialMenuDrawable.IconState.BURGER) {
                binding.drawer.openDrawer(START);
            } else {
                onBackPressed();
            }
        });
        binding.toolbar.inflateMenu(R.menu.menu_novelview);
        binding.toolbar.setOnMenuItemClickListener(this);

        binding.navView.setNavigationItemSelectedListener(this);

        binding.drawer.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        initFragment();
    }

    @Override
    public void onBackPressed() {
        isNovelTableView = false;
        int stack = fragmentManager.getBackStackEntryCount();
        if (stack == 1) {
            fragmentManager.popBackStack();
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
            binding.toolbar.setTitle(titleStack.pop());
            binding.navView.setCheckedItem(R.id.nav_ranking);
        } else if (stack > 1) {
            fragmentManager.popBackStack();
            binding.toolbar.setTitle(titleStack.pop());
        } else if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.download) {
            if (!isNovelTableView) {
                Toast.makeText(this, "小説目次ページを開いてから押してください", Toast.LENGTH_LONG).show();
                return true;
            }

            DownloadUtils downloadUtils = new DownloadUtils() {
                @Override
                public void onDownloadSuccess(NovelDownloadDialogFragment dialog, final Novel novel) {
                    dialog.dismiss();

                    OkCancelDialogFragment okCancelDialog = OkCancelDialogFragment.newInstance(
                        "ダウンロード完了", "ダウンロードしました。", (dialog1, which) -> dialog1.dismiss());
                    okCancelDialog.show(getSupportFragmentManager(), "okcansel");
                }

                @Override
                public void onDownloadError(NovelDownloadDialogFragment dialog) {
                    Log.d(TAG, "onDownloadError: ");

                    dialog.dismiss();
                }
            };
            downloadUtils.novelDownlaod(downloadTargetNovel.getNovelDetail(), getSupportFragmentManager(), this);
            return true;
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_ranking: {
                binding.toolbar.setTitle("ランキング");
                binding.navView.setCheckedItem(R.id.nav_ranking);
                String[] types = new String[]{
                    RankingType.DAILY.toString(), RankingType.WEEKLY.toString(),
                    RankingType.MONTHLY.toString(), RankingType.QUARTET.toString(), "all"};
                String[] titles = new String[]{"日間", "週間", "月間", "四半期", "累計"};
                Fragment fragment = RankingViewPagerFragment.newInstance(types, titles);
                fragmentManager.beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .commit();
                break;
            }
            case R.id.nav_bookmark: {
                binding.toolbar.setTitle("しおり");
                binding.navView.setCheckedItem(R.id.nav_bookmark);
                BookmarkRecyclerViewFragment fragment = BookmarkRecyclerViewFragment.newInstance();
                fragmentManager.beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .commit();
                break;
            }
            case R.id.nav_download: {
                binding.toolbar.setTitle("ダウンロード済み小説");
                binding.navView.setCheckedItem(R.id.nav_download);
                DownloadedRecyclerViewFragment fragment = DownloadedRecyclerViewFragment.newInstance();
                fragmentManager.beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .commit();
                break;
            }
            case R.id.nav_search: {
                binding.toolbar.setTitle("検索");
                binding.navView.setCheckedItem(R.id.nav_search);
                SearchFragment fragment = SearchFragment.newInstance();
                fragmentManager.beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .commit();
                break;
            }
            case R.id.nav_setting: {
                binding.navView.setCheckedItem(R.id.nav_setting);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_feedback: {
                DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
                    dialog.dismiss();

                    Intent intent = null;
                    switch (which) {
                        case 0: {
                            String url = "http://twitter.com/share?screen_name=narou_time&hashtags=なろうTime";
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            break;
                        }
                        case 1: {
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.nashihara.naroureader"));
                            break;
                        }
                    }

                    if (intent != null) {
                        startActivity(intent);
                    }
                };
                ListDailogFragment fragment =
                    ListDailogFragment.newInstance("フィードバック", new String[]{"Twitter", "Google Play Store"}, onClickListener);
                fragment.show(getSupportFragmentManager(), "list");
                break;
            }
        }

        binding.drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void initFragment() {
        Fragment fragment;

        if (NetworkUtils.isOnline(this)) {
            String[] types = new String[]{
                RankingType.DAILY.toString(), RankingType.WEEKLY.toString(),
                RankingType.MONTHLY.toString(), RankingType.QUARTET.toString(), "all"};
            String[] titles = new String[]{"日間", "週間", "月間", "四半期", "累計"};
            fragment = RankingViewPagerFragment.newInstance(types, titles);
            binding.toolbar.setTitle("ランキング");
        }
        else {
            fragment = DownloadedRecyclerViewFragment.newInstance();
            binding.toolbar.setTitle("ダウンロード済み小説");
        }

        fragmentManager.beginTransaction()
            .add(R.id.main_container, fragment)
            .commit();
    }

    @Override
    public void replaceFragment(Fragment fragment, String title, NovelItem item) {
        if (fragment == null) {
            return;
        }

        if (fragment instanceof NovelTableRecyclerViewFragment) {
            isNovelTableView = true;
            downloadTargetNovel = item;
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        }
        if (fragment instanceof SearchRecyclerViewFragment) {
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        }

        titleStack.push(binding.toolbar.getTitle());
        binding.toolbar.setTitle(title);
        fragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit();
    }

    @Override
    public void onSelect(String ncode, int totalPage, int page, String title, String writer, String bodyTitle) {
        Intent intent = new Intent(this, NovelViewActivity.class);
        intent.putExtra("ncode", ncode);
        intent.putExtra("page", page);
        intent.putExtra("title", title);
        intent.putExtra("writer", writer);
        intent.putExtra("bodyTitle", bodyTitle);
        intent.putExtra("totalPage", totalPage);
        startActivity(intent);
    }
}
