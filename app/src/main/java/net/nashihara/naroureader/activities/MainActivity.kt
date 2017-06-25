package net.nashihara.naroureader.activities

import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import com.balysv.materialmenu.MaterialMenuDrawable

import net.nashihara.naroureader.R
import net.nashihara.naroureader.databinding.ActivityMainBinding
import net.nashihara.naroureader.fragments.BookmarkRecyclerViewFragment
import net.nashihara.naroureader.fragments.DownloadedRecyclerViewFragment
import net.nashihara.naroureader.fragments.NovelTableRecyclerViewFragment
import net.nashihara.naroureader.fragments.RankingViewPagerFragment
import net.nashihara.naroureader.fragments.SearchFragment
import net.nashihara.naroureader.fragments.SearchRecyclerViewFragment
import net.nashihara.naroureader.listeners.FragmentTransactionListener
import net.nashihara.naroureader.entities.NovelItem
import net.nashihara.naroureader.utils.DownloadUtils
import net.nashihara.naroureader.utils.NetworkUtils
import net.nashihara.naroureader.widgets.ListDialogFragment
import net.nashihara.naroureader.widgets.NovelDownloadDialogFragment
import net.nashihara.naroureader.widgets.OkCancelDialogFragment

import java.util.Stack

import narou4j.entities.Novel

import android.support.v4.view.GravityCompat.START

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        FragmentTransactionListener, NovelTableRecyclerViewFragment.OnNovelSelectionListener, Toolbar.OnMenuItemClickListener {

    private var binding: ActivityMainBinding? = null

    private val TAG = MainActivity::class.java.simpleName

    private var fragmentManager: FragmentManager? = null

    private val titleStack = Stack<CharSequence>()

    private var materialMenu: MaterialMenuDrawable? = null

    private var isNovelTableView = false

    private var downloadTargetNovel: NovelItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        fragmentManager = supportFragmentManager

        materialMenu = MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN)
        materialMenu!!.animateIconState(MaterialMenuDrawable.IconState.BURGER)
        binding!!.toolbar.navigationIcon = materialMenu
        binding!!.toolbar.setNavigationOnClickListener { v ->
            if (materialMenu!!.iconState == MaterialMenuDrawable.IconState.BURGER) {
                binding!!.drawer.openDrawer(START)
            } else {
                onBackPressed()
            }
        }
        binding!!.toolbar.inflateMenu(R.menu.menu_novelview)
        binding!!.toolbar.setOnMenuItemClickListener(this)

        binding!!.navView.setNavigationItemSelectedListener(this)

        binding!!.drawer.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))

        initFragment()
    }

    override fun onBackPressed() {
        isNovelTableView = false
        val stack = fragmentManager!!.backStackEntryCount
        if (stack == 1) {
            fragmentManager!!.popBackStack()
            materialMenu!!.animateIconState(MaterialMenuDrawable.IconState.BURGER)
            binding!!.toolbar.title = titleStack.pop()
            binding!!.navView.setCheckedItem(R.id.nav_ranking)
        } else if (stack > 1) {
            fragmentManager!!.popBackStack()
            binding!!.toolbar.title = titleStack.pop()
        } else if (binding!!.drawer.isDrawerOpen(GravityCompat.START)) {
            binding!!.drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.download) {
            if (!isNovelTableView) {
                Toast.makeText(this, "小説目次ページを開いてから押してください", Toast.LENGTH_LONG).show()
                return true
            }

            val downloadUtils = object : DownloadUtils() {
                override fun onDownloadSuccess(dialog: NovelDownloadDialogFragment, novel: Novel) {
                    dialog.dismiss()

                    val okCancelDialog = OkCancelDialogFragment.newInstance(
                            "ダウンロード完了", "ダウンロードしました。") { dialog1, which -> dialog1.dismiss() }
                    okCancelDialog.show(supportFragmentManager, "okcansel")
                }

                override fun onDownloadError(dialog: NovelDownloadDialogFragment) {
                    Log.d(TAG, "onDownloadError: ")

                    dialog.dismiss()
                }
            }
            downloadUtils.novelDownlaod(downloadTargetNovel!!.novelDetail, supportFragmentManager, this)
            return true
        }

        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.nav_ranking -> {
                binding!!.toolbar.title = "ランキング"
                binding!!.navView.setCheckedItem(R.id.nav_ranking)
                val fragment = RankingViewPagerFragment.newInstance()
                fragmentManager!!.beginTransaction()
                        .replace(R.id.main_container, fragment)
                        .commit()
            }
            R.id.nav_bookmark -> {
                binding!!.toolbar.title = "しおり"
                binding!!.navView.setCheckedItem(R.id.nav_bookmark)
                val fragment = BookmarkRecyclerViewFragment.newInstance()
                fragmentManager!!.beginTransaction()
                        .replace(R.id.main_container, fragment)
                        .commit()
            }
            R.id.nav_download -> {
                binding!!.toolbar.title = "ダウンロード済み小説"
                binding!!.navView.setCheckedItem(R.id.nav_download)
                val fragment = DownloadedRecyclerViewFragment.newInstance()
                fragmentManager!!.beginTransaction()
                        .replace(R.id.main_container, fragment)
                        .commit()
            }
            R.id.nav_search -> {
                binding!!.toolbar.title = "検索"
                binding!!.navView.setCheckedItem(R.id.nav_search)
                val fragment = SearchFragment.newInstance()
                fragmentManager!!.beginTransaction()
                        .replace(R.id.main_container, fragment)
                        .commit()
            }
            R.id.nav_setting -> {
                binding!!.navView.setCheckedItem(R.id.nav_setting)
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_feedback -> {
                val onClickListener = DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()

                    var intent: Intent? = null
                    when (which) {
                        0 -> {
                            val url = "http://twitter.com/share?screen_name=narou_time&hashtags=なろうTime"
                            intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        }
                        1 -> {
                            intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.nashihara.naroureader"))
                        }
                    }

                    if (intent != null) {
                        startActivity(intent)
                    }
                }
                val fragment = ListDialogFragment.newInstance("フィードバック", arrayOf("Twitter", "Google Play Store"), onClickListener)
                fragment.show(supportFragmentManager, "list")
            }
        }

        binding!!.drawer.closeDrawer(GravityCompat.START)
        return false
    }

    private fun initFragment() {
        val fragment: Fragment

        if (NetworkUtils.isOnline(this)) {
            fragment = RankingViewPagerFragment.newInstance()
            binding!!.toolbar.title = "ランキング"
        } else {
            fragment = DownloadedRecyclerViewFragment.newInstance()
            binding!!.toolbar.title = "ダウンロード済み小説"
        }

        fragmentManager!!.beginTransaction()
                .add(R.id.main_container, fragment)
                .commit()
    }

    override fun replaceFragment(fragment: Fragment?, title: String, item: NovelItem?) {
        if (fragment == null) {
            return
        }

        if (fragment is NovelTableRecyclerViewFragment) {
            isNovelTableView = true
            downloadTargetNovel = item
            materialMenu!!.animateIconState(MaterialMenuDrawable.IconState.ARROW)
        }
        if (fragment is SearchRecyclerViewFragment) {
            materialMenu!!.animateIconState(MaterialMenuDrawable.IconState.ARROW)
        }

        titleStack.push(binding!!.toolbar.title)
        binding!!.toolbar.title = title
        fragmentManager!!.beginTransaction()
                .replace(R.id.main_container, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onSelect(ncode: String, totalPage: Int, page: Int, title: String, writer: String, bodyTitle: String) {
        val intent = Intent(this, NovelViewActivity::class.java)
        intent.putExtra("ncode", ncode)
        intent.putExtra("page", page)
        intent.putExtra("title", title)
        intent.putExtra("writer", writer)
        intent.putExtra("bodyTitle", bodyTitle)
        intent.putExtra("totalPage", totalPage)
        startActivity(intent)
    }
}
