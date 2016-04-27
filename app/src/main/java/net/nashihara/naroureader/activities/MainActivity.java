package net.nashihara.naroureader.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ActivityMainBinding;
import net.nashihara.naroureader.fragments.NovelTableRecyclerViewFragment;
import net.nashihara.naroureader.fragments.RankingViewPagerFragment;
import net.nashihara.naroureader.listeners.OnFragmentReplaceListener;

import java.util.ArrayList;
import java.util.Stack;

import narou4j.enums.RankingType;

import static android.support.v4.view.GravityCompat.START;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentReplaceListener, NovelTableRecyclerViewFragment.OnNovelSelectionListener {

    ActivityMainBinding binding;
    private String TAG = MainActivity.class.getSimpleName();
    private FragmentManager manager;
    private Stack<String> titleStack = new Stack<>();

    private MaterialMenuDrawable materialMenu;

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

        binding.navView.setNavigationItemSelectedListener(this);

        binding.drawer.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        initFragment();
    }

    @Override
    public void onBackPressed() {
        int stack = manager.getBackStackEntryCount();
        if (stack == 1) {
            manager.popBackStack();
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
            binding.toolbar.setTitle(titleStack.pop());
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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_ranking: {
                binding.toolbar.setTitle("ランキング");
                binding.navView.setCheckedItem(R.id.nav_ranking);
                break;
            }
            case R.id.nav_bookmark: {
                binding.toolbar.setTitle("しおり");
                binding.navView.setCheckedItem(R.id.nav_bookmark);
                break;
            }
            case R.id.nav_search: {
                binding.toolbar.setTitle("検索");
                binding.navView.setCheckedItem(R.id.nav_search);
                break;
            }
            case R.id.nav_setting: {
                binding.toolbar.setTitle("設定");
                binding.navView.setCheckedItem(R.id.nav_setting);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_feedback: {
                binding.toolbar.setTitle("フィードバック");
                binding.navView.setCheckedItem(R.id.nav_feedback);
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
    public void onFragmentReplaceAction(Fragment fragment, String title) {
        if (fragment == null) {
            return;
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
    public void onSelect(String ncode, int page, String title, String writer, ArrayList<String> titles) {
        Intent intent = new Intent(this, NovelViewActivity.class);
        intent.putExtra("ncode", ncode);
        intent.putExtra("page", page);
        intent.putExtra("title", title);
        intent.putExtra("writer", writer);
        intent.putExtra("titles", titles);
        startActivity(intent);
    }
}
