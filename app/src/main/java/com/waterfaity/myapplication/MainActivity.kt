package com.waterfaity.myapplication

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.waterfairy.fileselector.SelectFileActivity
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), Adapter.OnItemClickListener, AudioSeekBar.OnProgressListener {
    override fun onSeekTo(current: Long) {
        MusicPlayService.seekTo(this, current.toInt());
    }

    val dataList: ArrayList<String> = ArrayList()
    var serviceConnection: ServiceConnection? = null
    var seekBar: AudioSeekBar? = null
    override fun onItemLongClick(pos: Int) {
        dataList.removeAt(pos)
        mRecyclerView?.adapter?.notifyDataSetChanged()
    }

    override fun onItemClick(pos: Int) {
        var adapter = mRecyclerView?.adapter as Adapter
        MusicPlayService.play(this, adapter.dataList[pos])
    }

    var mRecyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mRecyclerView = findViewById(R.id.recycler_view)
        seekBar = findViewById(R.id.seek_bar)
        seekBar?.setOnProgressListener(this)
        var i = 0
        while (i++ < 100) {
            dataList.add(i.toString())
        }
        mRecyclerView?.layoutManager = LinearLayoutManager(this)
//        mRecyclerView?.adapter = Adapter(this, dataList).setOnClickListener(this)

//        MusicPlayService.play(this, "/sdcard/test/audio/jjj.mp3")
        var intent: Intent = Intent(this, Class.forName("com.waterfaity.myapplication.MusicPlayService"))
        startService(intent)
//        startService(intent);
        serviceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {

            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//                Log.i("log", "----")
//                play();
                var binder: MusicPlayService.MyBinder = service as MusicPlayService.MyBinder
                binder.play(PlayBean("/sdcard/test/audio/jjj.mp3"), 10000)
                binder.setOnMp3PlayListener(object : Mp3Player.onMp3PlayListener {
                    override fun OnPlaying(current: Int, total: Int) {
                        seekBar?.setTotal(total.toLong())
                        seekBar?.setCurrentLen(current.toLong())
                    }

                    override fun onMp3PlayError(state: Int, message: String?) {

                    }

                    override fun onPlayStateChanged(state: Int, message: String?) {

                    }
                })
            }
        }

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)


        var list = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(list, 1001)
        }
//        MusicPlayService.play(this, "/sdcard/test/audio/jjj.mp3")
    }

    fun play(view: View) {
        MusicPlayService.playOrPause(this)
    }

    fun pre(view: View) {
        MusicPlayService.playPre(this)
    }

    fun next(view: View) {
        MusicPlayService.playNext(this)
    }

    fun double2(view: View) {
        MusicPlayService.setSpeed(this, Random().nextFloat() * 3)
    }

    var num: Int = 0
    fun type(view: View) {
     num++
        MusicPlayService.setPlayType(this, num%4)
        Log.i("log", (num%4).toString())
    }

    fun playList(view: View) {

        var intent: Intent = Intent(this, SelectFileActivity::class.java)

        startActivityForResult(intent, 1001)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            var selectFileList = data?.getSerializableExtra(SelectFileActivity.RESULT_DATA) as ArrayList<File>

            var dataList = arrayListOf<MusicPlayService.MusicBean>()
            for (file in selectFileList) {
                dataList.add(PlayBean(file.absolutePath))
            }
            mRecyclerView?.adapter = Adapter(this, dataList).setOnClickListener(this)



            MusicPlayService.setPlayList(this, dataList)
        }
    }


}
