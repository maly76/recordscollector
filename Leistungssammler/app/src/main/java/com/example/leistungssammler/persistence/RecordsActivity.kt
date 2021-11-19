/**
 *  MONKEY TEST
 *  .\adb.exe shell monkey -p com.example.leistungssammler -v 1000
 *  */

package com.example.leistungssammler.persistence

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.constraintlayout.widget.Constraints
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.leistungssammler.R
import com.example.leistungssammler.databinding.ActivityRecordsBinding
import com.example.leistungssammler.model.Record
import com.example.leistungssammler.viewmodels.RecordsViewModel
import com.example.leistungssammler.viewmodels.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList
import androidx.work.Constraints.Builder
import com.example.leistungssammler.model.Module
import com.example.leistungssammler.worker.UpdateModulesWorker
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.util.concurrent.TimeUnit

class RecordsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordsBinding
    private var selectedRecords = ArrayList<Record>()
    private lateinit var records: List<Record>
    private lateinit var recordDAO: RecordDAO
    private val recordsViewModel: RecordsViewModel by viewModels()
    private var pickerMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        var appStarted = false
        super.onCreate(savedInstanceState)

        initWorkManager()

        if (intent.action == Intent.ACTION_PICK) {
            pickerMode = true
        }

        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recordDAO = AppDatabase.getDb(this).recordDao()
        records = emptyList()
        var adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, records)

        recordsViewModel.records.observe(this){
            records = it
            if (!appStarted) {
                adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, records)
                binding.recordListView.adapter = adapter
                appStarted = true
            } else {
                if (!adapter.isEmpty) {
                    adapter.clear()
                }
                adapter.addAll(it)
            }
        }
        binding.recordListView.emptyView = binding.recordListEmptyView

        binding.recordListView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val record = parent.getItemAtPosition(position) as Record
            if (pickerMode) {
                Intent().let {
                    it.data = "content://com.example.leistungssammler/records/${record.id}".toUri()
                    setResult(RESULT_OK, it)
                    finish()
                }
            } else {
                val intent = Intent(this, RecordFormActivity::class.java)
                intent.putExtra(RECORD_ID, record.id)
                startActivity(intent)
            }
        }

        if (!pickerMode) {
            enableMultiSelecting()
        }
    }

    private fun initWorkManager() {
        if(getFlag(FIRST_START, true) && !checkConnectivity()) {
            val modules = getLocalModules()
            val moduleDAO = AppDatabase.getDb(this).moduleDao()
            lifecycleScope.launch(Dispatchers.IO){
                moduleDAO.deleteAll()
                moduleDAO.persistAll(modules)
            }
            setFlag(FIRST_START, false)
        }

        val constraints = Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest =
        PeriodicWorkRequest.Builder(
            UpdateModulesWorker::class.java,
            30,
            TimeUnit.DAYS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("update modules", ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }

    @ExperimentalSerializationApi
    private fun getLocalModules(): List<Module> {
        val file = applicationContext.resources.openRawResource(R.raw.modules)
        val modules = Json.decodeFromStream<List<Module>>(file)
        file.close()
        return modules
    }

    private fun checkConnectivity(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = cm.activeNetwork ?: return false
        val activeNetwork = cm.getNetworkCapabilities(networkCapabilities) ?: return false
        return when{
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else                                                               -> false
        }
    }

    private fun setProgressBarVisible(visible: Boolean = true) {
        binding.statsProgressBar.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun enableMultiSelecting() {
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
            .setTitle(R.string.sure)
            .setMessage(R.string.delete_msg)
            .setNegativeButton(R.string.close, null)
            .setPositiveButton(R.string.delete) { _,_ ->
                lifecycleScope.launch(Dispatchers.IO){
                    recordDAO.deleteAll(selectedRecords)
                    withContext(Dispatchers.Main) {
                        mode.finish()
                    }
                }
            }
            .show()
        return true
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!pickerMode) {
            menuInflater.inflate(R.menu.records, menu)
        }
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
                showStatistic()
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

    private fun showStatistic(){
        recordsViewModel.statistic.observe(this) { result ->
            when (result.status) {
                Result.Status.IN_PROGRESS -> setProgressBarVisible()
                Result.Status.SUCCESS -> {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.statistic)
                        .setMessage(result.stats.toString())
                        .setNeutralButton(R.string.close, null)
                        .show()
                    recordsViewModel.statistic.removeObservers(this)
                    setProgressBarVisible(false)
                }
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun setFlag(key: String, value: Boolean) {
        getSharedPreferences("prefs", MODE_PRIVATE).edit().putBoolean(key, value).apply()
    }

    private fun getFlag(key: String, defVal: Boolean): Boolean {
        return getSharedPreferences("prefs", MODE_PRIVATE).getBoolean(key, defVal)
    }

    companion object {
        const val RECORD_ID = "ID"
        const val FIRST_START = "first_start"
    }
}
