/**
 *  MONKEY TEST
 *  .\adb.exe shell monkey -p com.example.leistungssammler -v 1000
 *  */

package com.example.leistungssammler.persistence

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.net.toUri
import androidx.room.Room
import com.example.leistungssammler.R
import com.example.leistungssammler.databinding.ActivityRecordsBinding
import com.example.leistungssammler.model.Record
import com.example.leistungssammler.model.Stats
import kotlin.collections.ArrayList

class RecordsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordsBinding
    private var selectedRecords = ArrayList<Record>()
    private lateinit var records: List<Record>
    private lateinit var adapter: ArrayAdapter<Record>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        records = RecordDAO.get(this).findAll()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, records)
        binding.recordListView.adapter = adapter
        binding.recordListView.emptyView = binding.recordListEmptyView
    }

    override fun onRestart() {
        refreshRecords()
        super.onRestart()
    }

    override fun onStart() {
        super.onStart()

        binding.recordListView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val record = parent.getItemAtPosition(position) as Record
            val intent = Intent(this, RecordFormActivity::class.java)
            intent.putExtra("RECORD", record)
            startActivity(intent)
        }

        binding.recordListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        binding.recordListView.setMultiChoiceModeListener (object : AbsListView.MultiChoiceModeListener {

            override fun onItemCheckedStateChanged(mode: ActionMode, position: Int,
                                                   id: Long, checked: Boolean) {
                if (!checked) {
                    selectedRecords.remove(records[position])
                } else {
                    selectedRecords.add(records[position])
                }
                mode.title = "${selectedRecords.size} ausgewählt"
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when(item.itemId) {
                    R.id.delete_btn -> {
                        confirmDelete(mode)
                    }
                    R.id.send_btn -> {
                        sendRecords()
                        mode.finish()
                    }
                }
                return false
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                menuInflater.inflate(R.menu.updatemenu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                selectedRecords.clear()
            }
        })
    }

    private fun confirmDelete(mode: ActionMode): Boolean {
        AlertDialog.Builder(this)
            .setTitle(R.string.statistic)
            .setMessage(R.string.delete_msg)
            .setNegativeButton(R.string.close, null)
            .setPositiveButton(R.string.delete) { _,_ ->
                selectedRecords.forEach { RecordDAO.get(this).delete(it) }
                setFlag(CHANGED_FLAG, true)
                refreshRecords()
                mode.finish()
            }
            .show()
        return true
    }

    private fun refreshRecords() {
        if (getFlag(CHANGED_FLAG)) {
            adapter.clear()
            records = RecordDAO.get(this).findAll()
            adapter.addAll(records)
            setFlag(CHANGED_FLAG, false)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun sendRecords(): Boolean {
        var mailBody = "Hier meine Leistungen\n"
        selectedRecords.forEach{ mailBody += "* $it \n" }
        val mailSubject = "Leistungen ${selectedRecords.size}"
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, mailSubject)
            putExtra(Intent.EXTRA_TEXT, mailBody)
        }

        if (intent.resolveActivity(packageManager) != null) {
            intent.setPackage("com.google.android.gm")
            startActivity(intent)
        } else {
            AlertDialog.Builder(this)
                .setTitle("App not founded!")
                .setMessage("Sorry, the Gmail-App is not installed!")
                .setNeutralButton(R.string.close, null)
                .show()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.records, menu)
        return true
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.add_record_btn -> {
                val intent = Intent(this, RecordFormActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.stat_show_btn -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.statistic)
                    .setMessage(Stats(records).toString())
                    .setNeutralButton(R.string.close, null)
                    .show()
                true
            }
            R.id.module_catalog_btn -> {
                Intent(Intent.ACTION_VIEW).let {
                    it.data = "https://www.thm.de/organizer/...".toUri()
                    startActivity(it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun setFlag(key: String, value: Boolean) {
        getSharedPreferences("prefs", MODE_PRIVATE).edit().putBoolean(key, value).apply()
    }

    private fun getFlag(key: String): Boolean {
        return getSharedPreferences("prefs", MODE_PRIVATE).getBoolean(key, false)
    }

    companion object {
        const val CHANGED_FLAG = "CHANGED"
    }
}