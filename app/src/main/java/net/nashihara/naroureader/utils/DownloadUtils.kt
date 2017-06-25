package net.nashihara.naroureader.utils

import android.content.Context
import android.content.DialogInterface
import android.support.v4.app.FragmentManager
import android.util.Log

import com.google.firebase.crash.FirebaseCrash

import net.nashihara.naroureader.widgets.NovelDownloadDialogFragment
import net.nashihara.naroureader.widgets.OkCancelDialogFragment
import net.nashihara.naroureader.entities.Novel4Realm
import net.nashihara.naroureader.entities.NovelBody4Realm
import net.nashihara.naroureader.entities.NovelTable4Realm

import java.io.IOException

import io.realm.Realm
import narou4j.Narou
import narou4j.entities.Novel
import narou4j.entities.NovelBody
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers

abstract class DownloadUtils {
    private var downloadDialog: NovelDownloadDialogFragment? = null
    private lateinit var manager: FragmentManager
    private var realm: Realm? = null
    private var mContext: Context? = null
    private var novel: Novel? = null

    fun novelDownload(novel: Novel, manager: FragmentManager, context: Context) {
        mContext = context
        this.novel = novel
        this.manager = manager

        val okCancelDialog = OkCancelDialogFragment.newInstance("小説ダウンロード", novel.title + "をダウンロードしますか？", DialogInterface.OnClickListener { dialog, which ->
            if (OkCancelDialogFragment.CANCEL == which) {
                return@OnClickListener
            }
            dialog.dismiss()

            checkNovel()
        })
        okCancelDialog.show(manager, "okcansel")
    }

    private fun checkNovel() {
        realm = RealmUtils.getRealm(mContext)
        val novel4Realm = realm!!.where(Novel4Realm::class.java).equalTo("ncode", novel!!.ncode.toLowerCase()).findFirst()
        if (novel4Realm != null) {
            if (novel4Realm.isDownload) {
                downloaded(manager)
                return
            }
        } else {
            realm!!.executeTransaction { transaction ->
                val saveNovel = Novel4Realm()
                saveNovel.ncode = novel!!.ncode.toLowerCase()
                saveNovel.title = novel!!.title
                saveNovel.writer = novel!!.writer
                saveNovel.story = novel!!.story
                saveNovel.totalPage = novel!!.allNumberOfNovel
                transaction.copyToRealmOrUpdate(saveNovel)
            }
        }
        realm!!.close()

        downloadDialog = NovelDownloadDialogFragment.newInstance(novel!!.allNumberOfNovel, "小説ダウンロード", novel!!.title + "をダウンロード中")
        downloadDialog?.show(manager, "download")
        downloadTable()
    }

    private fun downloaded(manager: FragmentManager) {
        val okCancelDialog = OkCancelDialogFragment.newInstance(
                "小説ダウンロード", "すでにこの小説はダウンロード済みです", DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })

        okCancelDialog.show(manager, "okcancel")
    }

    private fun downloadTable() {
        Observable.create(Observable.OnSubscribe<List<NovelBody>> { subscriber ->
            val narou = Narou()
            try {
                subscriber.onNext(narou.getNovelTable(novel!!.ncode.toLowerCase()))
            } catch (e: IOException) {
                subscriber.onError(e)
            }

            subscriber.onCompleted()
        }).subscribeOn(Schedulers.io()).subscribe(object : Subscriber<List<NovelBody>>() {
            override fun onCompleted() {
                novel?.let { downloadBody(it.allNumberOfNovel) }
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: ", e.fillInStackTrace())
                FirebaseCrash.report(e)
                downloadDialog?.let { onDownloadError(it) }
            }

            override fun onNext(novelBodies: List<NovelBody>) {
                storeTable(novelBodies)
                downloadDialog?.progress = 1
            }
        })
    }

    private fun downloadBody(totalPage: Int) {
        Observable.create(Observable.OnSubscribe<NovelBody> { subscriber ->
            val narou = Narou()

            try {
                realm = RealmUtils.getRealm(mContext)
                for (i in 1..totalPage) {
                    subscriber.onNext(narou.getNovelBody(novel!!.ncode.toLowerCase(), i))
                }
                realm?.close()
            } catch (e: IOException) {
                subscriber.onError(e)
            }

            subscriber.onCompleted()
        }).subscribe(object : Subscriber<NovelBody>() {
            override fun onCompleted() {
                updateIsDownload()
                downloadDialog?.let { dialog -> novel?.let { novel ->
                    onDownloadSuccess(dialog, novel)
                }}
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: ", e.fillInStackTrace())
                FirebaseCrash.report(e)
                downloadDialog?.let { onDownloadError(it) }
                realm!!.close()
            }

            override fun onNext(novelBody: NovelBody) {
                realm!!.beginTransaction()
                val body4Realm = realm!!.createObject(NovelBody4Realm::class.java)
                body4Realm.ncode = novelBody.ncode
                body4Realm.page = novelBody.page
                body4Realm.title = novelBody.title
                body4Realm.body = novelBody.body
                realm!!.commitTransaction()
                downloadDialog!!.progress = novelBody.page + 1
            }
        })
    }

    private fun updateIsDownload() {
        realm = RealmUtils.getRealm(mContext)
        val novel4Realm = realm!!.where(Novel4Realm::class.java).equalTo("ncode", novel!!.ncode.toLowerCase()).findFirst()
        realm!!.beginTransaction()
        novel4Realm.isDownload = true
        realm!!.commitTransaction()
        realm!!.close()
    }

    private fun storeTable(novelBodies: List<NovelBody>) {
        realm = RealmUtils.getRealm(mContext)
        realm!!.beginTransaction()
        for (i in novelBodies.indices) {
            val targetTable = novelBodies[i]
            val storeTable = realm!!.createObject(NovelTable4Realm::class.java)

            storeTable.tableNumber = i

            if (targetTable.isChapter) {
                storeTable.isChapter = true
                storeTable.title = targetTable.title
                storeTable.ncode = targetTable.ncode
            } else {
                storeTable.isChapter = false
                storeTable.title = targetTable.title
                storeTable.ncode = targetTable.ncode
                storeTable.page = targetTable.page
            }
        }
        realm!!.commitTransaction()
        realm!!.close()
    }

    abstract fun onDownloadSuccess(dialog: NovelDownloadDialogFragment, novel: Novel)
    abstract fun onDownloadError(dialog: NovelDownloadDialogFragment)

    companion object {
        private val TAG = DownloadUtils::class.java.simpleName
    }
}