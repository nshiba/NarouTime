package net.nashihara.naroureader.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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

import net.nashihara.naroureader.OnFragmentReplaceListener;
import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ActivityMainBinding;
import net.nashihara.naroureader.fragments.NovelTableRecyclerViewFragment;
import net.nashihara.naroureader.fragments.RankingViewPagerFragment;

import java.util.ArrayList;
import java.util.Stack;

import narou4j.enums.RankingType;

import static android.support.v4.view.GravityCompat.START;

public class MainActivity extends AppCompatActivity implements OnFragmentReplaceListener, NovelTableRecyclerViewFragment.OnNovelSelectionListener {
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

        binding.toolbar.setTitle(R.string.app_name);
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

        String[] types = new String[]{
                RankingType.DAILY.toString(), RankingType.WEEKLY.toString(),
                RankingType.MONTHLY.toString(), RankingType.QUARTET.toString(), "all"};
        String[] titles = new String[]{"日間", "週間", "月間", "四半期", "累計"};
        manager.beginTransaction()
                .add(R.id.main_container, RankingViewPagerFragment.newInstance(types, titles))
                .commit();
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
    public void onSelect(String ncode, int page, String title, ArrayList<String> titles) {
        Intent intent = new Intent(this, NovelViewActivity.class);
        intent.putExtra("ncode", ncode);
        intent.putExtra("page", page);
        intent.putExtra("title", title);
        intent.putExtra("titles", titles);
        startActivity(intent);
    }
}
