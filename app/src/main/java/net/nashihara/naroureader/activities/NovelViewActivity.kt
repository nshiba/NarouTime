package net.nashihara.naroureader.activities

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem

import com.balysv.materialmenu.MaterialMenuDrawable

import net.nashihara.naroureader.R
import net.nashihara.naroureader.databinding.ActivityNovelViewBinding
import net.nashihara.naroureader.fragments.NovelBodyFragment
import net.nashihara.naroureader.entities.Novel4Realm
import net.nashihara.naroureader.utils.RealmUtils
import net.nashihara.naroureader.adapters.NovelBodyFragmentViewPagerAdapter
import net.nashihara.naroureader.widgets.OkCancelDialogFragment

import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults

class NovelViewActivity : AppCompatActivity(), NovelBodyFragment.OnNovelBodyInteraction, Toolbar.OnMenuItemClickListener {
    private lateinit var binding: ActivityNovelViewBinding
    private var manager: FragmentManager? = null
    private var materialMenu: MaterialMenuDrawable? = null
    private var realm: Realm? = null

    private var bodyTitle: String? = null
    private var title: String? = null
    private var totalPage: Int = 0
    private var ncode: String? = null
    private var writer: String? = null

    private var pref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityNovelViewBinding>(this, R.layout.activity_novel_view)
        manager = supportFragmentManager
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        realm = RealmUtils.getRealm(this)

        val intent = intent
        ncode = intent.getStringExtra("ncode")
        val page = intent.getIntExtra("page", 1)

        title = intent.getStringExtra("title")
        writer = intent.getStringExtra("writer")
        bodyTitle = intent.getStringExtra("bodyTitle")
        totalPage = intent.getIntExtra("totalPage", 0)

        binding.toolbar.title = bodyTitle
        materialMenu = MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN)
        materialMenu!!.animateIconState(MaterialMenuDrawable.IconState.X)
        binding.toolbar.title = title
        binding.toolbar.navigationIcon = materialMenu
        binding.toolbar.setNavigationOnClickListener { v -> onBackPressed() }
        binding.toolbar.inflateMenu(R.menu.menu_novelbody)
        binding.toolbar.setOnMenuItemClickListener(this)

        val adapter = NovelBodyFragmentViewPagerAdapter(
                supportFragmentManager, ncode ?: "", title ?: "", totalPage)
        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = page - 1

        binding.fab.setOnClickListener { v ->
            title = binding.toolbar.title as String
            val builder = StringBuilder()
            builder.append(title)
            builder.append("にしおりをはさみますか？")
            val dialogFragment = OkCancelDialogFragment.newInstance("しおり", builder.toString(), DialogInterface.OnClickListener { _, which ->
                if (which == OkCancelDialogFragment.OK) {
                    bookmark(binding.viewPager.currentItem + 1)
                }
            })
            dialogFragment.show(supportFragmentManager, "okcansel")
        }

        val autoRemoveBookmark = pref!!.getBoolean(getString(R.string.auto_remove_bookmark), false)
        if (autoRemoveBookmark) {
            removeBookmark()
        }
    }

    override fun onBackPressed() {
        pref!!.edit().putBoolean(PREF_IS_HIDE, false).apply()

        val autoBookmark = pref!!.getBoolean(getString(R.string.auto_bookmark), false)
        Log.d(TAG, "onBackPressed: auto bookmark" + autoBookmark)
        if (autoBookmark) {
            bookmark(binding.viewPager.currentItem + 1)
        }

        finish()

        super.onBackPressed()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        Log.d(TAG, "dispatchKeyEvent: ")

        val volume_type = Integer.parseInt(pref!!.getString(getString(R.string.hardware_btn_volume), "0"))

        if (volume_type == 0) {
            return super.dispatchKeyEvent(event)
        }

        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            if (event.action != KeyEvent.ACTION_DOWN) {
                return super.dispatchKeyEvent(event)
            }

            val diff: Int
            if (volume_type == 1) {
                diff = 1
            } else {
                diff = -1
            }

            val nowPage = binding.viewPager.currentItem
            if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                binding.viewPager.currentItem = nowPage + diff
            }
            if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                binding.viewPager.currentItem = nowPage - diff
            }
            return true
        } else {
            return super.dispatchKeyEvent(event)
        }
    }

    override fun onNovelBodyLoadAction(body: String, nextPage: Int, bodyTitle: String) {
        Log.d(TAG, "onNovelBodyLoadAction: " + nextPage)
        binding.viewPager.currentItem = nextPage - 1
    }

    override fun getNovel4RealmInstance(): Novel4Realm {
        if (realm?.isClosed ?: false) {
            realm = RealmUtils.getRealm(this)
        }

        val novel4Realm = Novel4Realm()
        novel4Realm.title = title
        novel4Realm.writer = writer
        novel4Realm.ncode = ncode
        novel4Realm.totalPage = totalPage
        Log.d(TAG, "getNovel4RealmInstance: " + novel4Realm.toString())
        realm?.executeTransaction { realmTransaction -> realmTransaction.copyToRealmOrUpdate(novel4Realm) }
        return novel4Realm
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.body_setting -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        return true
    }

    private val realmResult: RealmResults<Novel4Realm>
        get() {
            if (realm!!.isClosed) {
                realm = RealmUtils.getRealm(this)
            }

            val query = realm!!.where(Novel4Realm::class.java)
            query.equalTo("ncode", ncode)
            val results = query.findAll()
            return results
        }

    fun bookmark(page: Int) {
        val novel4Realm = Novel4Realm()
        novel4Realm.title = title
        novel4Realm.writer = writer
        novel4Realm.ncode = ncode
        novel4Realm.totalPage = totalPage
        novel4Realm.bookmark = page
        Log.d(TAG, "getNovel4RealmInstance: " + novel4Realm.toString())
        realm!!.executeTransaction { realmTransaction -> realmTransaction.copyToRealmOrUpdate(novel4Realm) }
        //        RealmResults<Novel4Realm> results = getRealmResult();
        //
        //        if (results.size() != 0) {
        //            realm.beginTransaction();
        //
        //            Novel4Realm novel4Realm = results.get(0);
        //            novel4Realm.setBookmark(page);
        //            novel4Realm.setTotalPage(totalPage);
        //
        //            realm.commitTransaction();
        //        }
        //        else {
        //            Novel4Realm novel4Realm = getNovel4RealmInstance();
        //            realm.beginTransaction();
        //            novel4Realm.setBookmark(page);
        //            realm.commitTransaction();
        //        }
    }

    fun removeBookmark() {
        val results = realmResult

        if (results.size != 0) {
            realm!!.beginTransaction()

            val novel4Realm = results[0]
            val bookmarkPage = novel4Realm.bookmark

            if (bookmarkPage > 0) {
                novel4Realm.bookmark = 0
                realm!!.commitTransaction()
            } else {
                realm!!.cancelTransaction()
            }
        }
    }

    companion object {


        private val TAG = NovelViewActivity::class.java.simpleName
        private val PREF_IS_HIDE = "is_hide"
    }
}