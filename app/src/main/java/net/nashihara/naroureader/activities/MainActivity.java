package net.nashihara.naroureader.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
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
import android.view.View;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;

import net.nashihara.naroureader.DownloadUtil;
import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ActivityMainBinding;
import net.nashihara.naroureader.dialogs.ListDailogFragment;
import net.nashihara.naroureader.dialogs.NovelDownloadDialogFragment;
import net.nashihara.naroureader.dialogs.OkCancelDialogFragment;
import net.nashihara.naroureader.entities.NovelItem;
import net.nashihara.naroureader.fragments.BookmarkRecyclerViewFragment;
import net.nashihara.naroureader.fragments.DownloadedRecyclerViewFragment;
import net.nashihara.naroureader.fragments.NovelTableRecyclerViewFragment;
import net.nashihara.naroureader.fragments.RankingViewPagerFragment;
import net.nashihara.naroureader.listeners.OnFragmentReplaceListener;

import java.util.Stack;

import narou4j.entities.Novel;
import narou4j.enums.RankingType;

import static android.support.v4.view.GravityCompat.START;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentReplaceListener, NovelTableRecyclerViewFragment.OnNovelSelectionListener, Toolbar.OnMenuItemClickListener {

    ActivityMainBinding binding;
    private String TAG = MainActivity.class.getSimpleName();
    private FragmentManager manager;
    private Stack<String> titleStack = new Stack<>();

    private MaterialMenuDrawable materialMenu;

    private boolean isNovelTableView = false;
    private NovelItem downloadTargetNovel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        manager = getSupportFragmentManager();

        binding.toolbar.setTitle("ランキング");
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
        binding.toolbar.setNavigationIcon(materialMenu);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stack = manager.getBackStackEntryCount();
                Log.d(TAG, "onClick: stack -> " + stack);
                if (stack == 0) {
                    binding.drawer.openDrawer(START);
                } else {
                    onBackPressed();
                }
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
        int stack = manager.getBackStackEntryCount();
        if (stack == 1) {
            manager.popBackStack();
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
            binding.toolbar.setTitle(titleStack.pop());
            binding.navView.setCheckedItem(R.id.nav_ranking);
        } else if (stack > 1) {
            manager.popBackStack();
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

            DownloadUtil downloadUtil = new DownloadUtil() {
                @Override
                public void onDownloadSuccess(NovelDownloadDialogFragment dialog, final Novel novel) {
                    Log.d(TAG, "onSuccess: ");
                    dialog.dismiss();

                    OkCancelDialogFragment okCancelDialog =
                            new OkCancelDialogFragment("ダウンロード完了", "ダウンロードしました。", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    okCancelDialog.show(getSupportFragmentManager(), "okcansel");
                }

                @Override
                public void onDownloadError(NovelDownloadDialogFragment dialog) {
                    Log.d(TAG, "onDownloadError: ");

                    dialog.dismiss();
                }
            };
            downloadUtil.novelDownlaod(downloadTargetNovel.getNovelDetail(), getSupportFragmentManager(), this);
            return true;
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_ranking: {
                binding.navView.setCheckedItem(R.id.nav_ranking);
                break;
            }
            case R.id.nav_bookmark: {
                binding.navView.setCheckedItem(R.id.nav_bookmark);
                BookmarkRecyclerViewFragment fragment = BookmarkRecyclerViewFragment.newInstance();
                onFragmentReplaceAction(fragment, "しおり", null);
                break;
            }
            case R.id.nav_download: {
                binding.toolbar.setTitle("ダウンロード済み小説");
                binding.navView.setCheckedItem(R.id.nav_download);
                DownloadedRecyclerViewFragment fragment = DownloadedRecyclerViewFragment.newInstance();
                onFragmentReplaceAction(fragment, "ダウンロード済み小説", null);
                break;
            }
//            case R.id.nav_search: {
//                binding.toolbar.setTitle("検索");
//                binding.navView.setCheckedItem(R.id.nav_search);
//                break;
//            }
            case R.id.nav_setting: {
                binding.navView.setCheckedItem(R.id.nav_setting);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_feedback: {
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Intent intent = null;
                        switch (which) {
                            case 0: {
                                String url = "http://twitter.com/share?hashtags=なろうTime";
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
        String[] types = new String[]{
                RankingType.DAILY.toString(), RankingType.WEEKLY.toString(),
                RankingType.MONTHLY.toString(), RankingType.QUARTET.toString(), "all"};
        String[] titles = new String[]{"日間", "週間", "月間", "四半期", "累計"};
        manager.beginTransaction()
                .add(R.id.main_container, RankingViewPagerFragment.newInstance(types, titles))
                .commit();
    }

    @Override
    public void onFragmentReplaceAction(Fragment fragment, String title, NovelItem item) {
        if (fragment == null) {
            return;
        }

        if (fragment instanceof NovelTableRecyclerViewFragment) {
            isNovelTableView = true;
            downloadTargetNovel = item;
        }

        titleStack.push((String) binding.toolbar.getTitle());
        binding.toolbar.setTitle(title);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        manager.beginTransaction()
                .replace(R.id.main_container, fragment)
                .addToBackStack(null)
                .commit();

    }

    public Drawable getDrawableResouces(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getDrawable(id);
        }
        else {
            return ContextCompat.getDrawable(this, R.drawable.ic_menu_black_24dp);
        }
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
