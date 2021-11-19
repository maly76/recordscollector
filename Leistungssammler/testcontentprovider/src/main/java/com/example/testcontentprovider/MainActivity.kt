package com.example.testcontentprovider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val uri = "content://com.example.leistungssammler/records".toUri()
        val cr = contentResolver

        val projection = arrayOf("id", "moduleName")
        cr.query(uri, projection, null, null, "moduleName")?.let { c ->
            while (c.moveToNext()){
                Log.i(TAG, "id: ${c.getLong(0)} name: ${c.getString(1)}")
            }
            c.close()
        }
    }
}