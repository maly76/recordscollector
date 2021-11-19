package com.example.leistungssammler.persistence

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.leistungssammler.R
import com.example.leistungssammler.databinding.ActivityRecordFormBinding
import android.R.layout.simple_dropdown_item_1line
import android.R.layout.simple_spinner_dropdown_item
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.leistungssammler.model.Module
import com.example.leistungssammler.model.Record
import com.example.leistungssammler.viewmodels.RecordsFormViewModel
import com.example.leistungssammler.viewmodels.RecordsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RecordFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordFormBinding
    private lateinit var record: Record
    private val recordDAO = AppDatabase.getDb(this).recordDao()
    private val moduleDAO = AppDatabase.getDb(this).moduleDao()
    private val recordsFormViewModel: RecordsFormViewModel by viewModels()
    private lateinit var searchedModules: List<Module>
    private lateinit var listView: ListView
    private lateinit var modulesDialog: AlertDialog

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordFormBinding.inflate(layoutInflater)
        listView = ListView(this)
        searchedModules = emptyList()
        recordsFormViewModel.modules.observe(this) {
            searchedModules = it
            val modulesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, searchedModules)
            listView.adapter = modulesAdapter
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val module = parent.getItemAtPosition(position) as Module
            setInformations(null, module)
            modulesDialog.dismiss()
        }

        if (recordsFormViewModel.years.isEmpty() || recordsFormViewModel.names.isEmpty()) {
            recordsFormViewModel.names = resources.getStringArray(R.array.module_names)
            recordsFormViewModel.years = resources.getStringArray(R.array.years)
        }

        if (recordsFormViewModel.record == null) {
            if (intent.hasExtra(RecordsActivity.RECORD_ID)) {
                val recordID = intent.getIntExtra(RecordsActivity.RECORD_ID, -1)
                lifecycleScope.launch(Dispatchers.IO){
                    recordDAO.findById(recordID)?.let {
                        withContext(Dispatchers.Main) {
                            recordsFormViewModel.record = it
                            setInformations(it, null)
                        }
                    }
                }
            } else {
                record = Record()
            }
        } else {
            record = recordsFormViewModel.record!!
        }

        val adapter = ArrayAdapter(this, simple_dropdown_item_1line, recordsFormViewModel.names)
        binding.modulnameInput.setAdapter(adapter)
        binding.year.adapter = ArrayAdapter(this, simple_spinner_dropdown_item, recordsFormViewModel.years)

        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.recordsform, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.search_module_btn -> {
                openModulesDialog()
                Toast.makeText(this, "search module", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openModulesDialog() {
        if (listView.parent != null) {
            (listView.parent as ViewGroup).removeView(listView)
        }

        modulesDialog = AlertDialog.Builder(this)
            .setTitle(R.string.search_module)
            .setView(listView)
            .setNeutralButton(R.string.close, null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setInformations(_record: Record?, module: Module?) {
        var nr = ""; var name = ""; var isST = true; var crp = ""; var mark = ""; var isHW = true; var year = ""
        if (_record != null) {
            this.record = _record
            nr = _record.moduleNum
            name = _record.moduleName
            isST = _record.isSummerTerm
            crp = _record.crp.toString()
            mark = _record.mark.toString()
            isHW = _record.isHalfWeighted
            year = _record.year.toString()
        } else if (module != null) {
            nr = module.nr!!
            name = module.name!!
            crp = module.crp.toString()
            year = LocalDate.now().year.toString()
        }

        binding.modulnumberInput.setText(nr)
        binding.modulnameInput.setText(name)
        binding.ssCheckbox.isChecked = isST
        binding.year.setSelection(recordsFormViewModel.years.indexOf(year))
        binding.crpInput.setText(crp)
        binding.noteInput.setText(mark)
        binding.gewichtungCheck.isChecked = isHW
    }

    fun onSave(view: View) {
        if (record.id != null && !checkChangedOnUpdate(record)) {
            finish()
        } else {
            var isValid = true
            record.moduleNum = binding.modulnumberInput.text.toString().trim().ifEmpty {
                binding.modulnumberInput.error = getString(R.string.mod_num_not_empty)
                isValid = false
                ""
            }

            record.moduleName = binding.modulnameInput.text.toString().trim()
            record.crp = Integer.parseInt(binding.crpInput.text.toString().trim().ifEmpty {
                binding.crpInput.error = getString(R.string.crp_not_empty)
                isValid = false
                "0"
            })
            if (record.crp > 15) {
                binding.crpInput.error = getString(R.string.crp_greater_15)
                isValid = false
            } else if (record.crp < 3) {
                binding.crpInput.error = getString(R.string.crp_less_3)
                isValid = false
            }

            record.mark = Integer.parseInt(binding.noteInput.text.toString().trim().ifEmpty {
                binding.noteInput.error = getString(R.string.crp_less_3)
                isValid = false
                "0"
            })

            if (record.mark in 1..49) {
                binding.noteInput.error = getString(R.string.note_error)
                isValid = false
            }

            record.isSummerTerm = binding.ssCheckbox.isChecked
            record.year = Integer.parseInt(binding.year.selectedItem.toString())

            record.isHalfWeighted = binding.gewichtungCheck.isChecked

            if (isValid) {
                if (record.id == null) {
                    lifecycleScope.launch(Dispatchers.IO){
                        recordDAO.persist(record)
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.IO){
                        recordDAO.update(record)
                    }
                }
                finish()
            }
        }
    }

    private fun checkChangedOnUpdate(record: Record): Boolean {
        return binding.modulnumberInput.text.trim().toString() != record.moduleNum ||
                binding.modulnameInput.text.trim().toString() != record.moduleName ||
                binding.ssCheckbox.isChecked != record.isSummerTerm ||
                binding.year.selectedItem.toString() != record.year.toString() ||
                binding.crpInput.text.trim().toString() != record.crp.toString() ||
                binding.noteInput.text.trim().toString() != record.mark.toString() ||
                binding.gewichtungCheck.isChecked != record.isHalfWeighted
    }

    @SuppressLint("CommitPrefEdits")
    private fun setFlag(key: String, value: Boolean) {
        getSharedPreferences("prefs", MODE_PRIVATE).edit().putBoolean(key, value).apply()
    }
}