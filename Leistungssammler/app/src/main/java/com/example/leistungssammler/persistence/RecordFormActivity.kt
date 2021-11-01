package com.example.leistungssammler.persistence

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.example.leistungssammler.R
import com.example.leistungssammler.databinding.ActivityRecordFormBinding
import android.R.layout.simple_dropdown_item_1line
import android.R.layout.simple_spinner_dropdown_item
import android.annotation.SuppressLint
import android.util.Log
import com.example.leistungssammler.model.Record

class RecordFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordFormBinding
    private lateinit var record: Record

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordFormBinding.inflate(layoutInflater)

        val names = resources.getStringArray(R.array.module_names)
        val years = resources.getStringArray(R.array.years)

        val adapter = ArrayAdapter(this, simple_dropdown_item_1line, names)
        binding.modulnameInput.setAdapter(adapter)
        binding.year.adapter = ArrayAdapter(this, simple_spinner_dropdown_item, years)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val receivedRecord = intent.getSerializableExtra("RECORD") as? Record
        if (receivedRecord != null) {
            record = receivedRecord
            binding.modulnumberInput.setText(record.moduleNum)
            binding.modulnameInput.setText(record.moduleName)
            binding.ssCheckbox.isChecked = record.isSummerTerm
            binding.year.setSelection(resources.getStringArray(R.array.years).indexOf(record.year.toString()))
            binding.crpInput.setText(record.crp.toString())
            binding.noteInput.setText(record.mark.toString())
            binding.gewichtungCheck.isChecked = record.isHalfWeighted
        } else {
            record = Record()
        }
    }

    fun onSave(view: View) {
        if (record.id != -1 && !checkChangedOnUpdate(record)) {
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
                if (record.id == -1) {
                    RecordDAO.get(this).persist(record)
                } else {
                    RecordDAO.get(this).update(record)
                }
                setFlag(RecordsActivity.CHANGED_FLAG, true)
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