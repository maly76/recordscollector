package com.example.testpicker

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.name
    private val picker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { recordURI ->
        recordURI?.let{
            val uri = recordURI.data!!.data
            if (uri != null) {
                getRecord(uri)
            }
        }
    }

    private fun getRecord(uri: Uri) {
        val cr = contentResolver
        val projection = arrayOf("id", "moduleName")
        cr.query(uri, projection, null, null, "moduleName")?.let { c ->
            while (c.moveToNext()){
                Log.i(TAG, "id: ${c.getLong(0)} name: ${c.getString(1)}")
            }
            c.close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun pickRecord(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "text/plain"
        intent.component = ComponentName("com.example.leistungssammler", "com.example.leistungssammler.persistence.RecordsActivity")
        try { picker.launch(intent) } catch (e: ActivityNotFoundException) {
            Log.e("Error", "Activity ${intent.component!!.className} not found")
        }
    }
}