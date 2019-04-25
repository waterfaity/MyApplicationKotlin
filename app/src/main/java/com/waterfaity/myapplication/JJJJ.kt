package com.waterfaity.myapplication

import android.app.Activity
import android.content.Intent
import android.util.Log

import com.waterfairy.fileselector.SelectFileActivity

import java.io.File
import java.util.ArrayList

class JJJJ : Activity() {
    internal var intent = Intent(this, SelectFileActivity::class.java)

    internal var data = intent.getSerializableExtra("jjj") as ArrayList<File>

    init {

        val dataList = ArrayList<String>()
        for (i in 0..5) {
            dataList.add(i.toString() + "")
        }
        for (data in dataList) {
            Log.i(TAG, "instance initializer: $data")
        }
    }

    companion object {
        private val TAG = "jjjj"
    }
}
