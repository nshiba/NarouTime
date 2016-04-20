package net.nashihara.naroureader;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.nashihara.naroureader.databinding.ActivityMainBinding;
import net.nashihara.naroureader.fragments.RankingViewPagerFragment;

import narou4j.enums.RankingType;

public class MainActivity extends AppCompatActivity implements OnFragmentReplaceListener {
    ActivityMainBinding binding;
    private String TAG = MainActivity.class.getSimpleName();
    private FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);
        manager = getSupportFragmentManager();

        String[] types = new String[]{
                RankingType.DAILY.toString(), RankingType.WEEKLY.toString(),
                RankingType.MONTHLY.toString(), RankingType.QUARTET.toString(), "all"};
        String[] titles = new String[]{"日間", "週間", "月間", "四半期", "累計"};
        manager.beginTransaction()
                .replace(R.id.main_container, RankingViewPagerFragment.newInstance(types, titles))
                .commit();
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
    public void onFragmentReplaceAction(String str) {
        Log.d(TAG, "onFragmentReplaceAction: " + str);
//        manager.beginTransaction()
//                .replace(R.id.main_container, fragment)
//                .commit();
    }
}
