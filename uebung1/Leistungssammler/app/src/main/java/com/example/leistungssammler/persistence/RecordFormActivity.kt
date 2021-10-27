package com.example.leistungssammler.persistence

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.example.leistungssammler.R
import com.example.leistungssammler.databinding.ActivityRecordFormBinding
import android.R.layout.simple_dropdown_item_1line
import android.R.layout.simple_spinner_dropdown_item
import android.util.Log
import android.widget.Toast
import com.example.leistungssammler.model.Record

class RecordFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordFormBinding
    private val record = Record(0)

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

    fun onSave(view: View) {
        var isValid = true
        record.modulnummer = binding.modulnumberInput.text.toString().trim().ifEmpty {
            binding.modulnumberInput.error = getString(R.string.mod_num_not_empty)
            isValid = false
            ""
        }

        record.modulname = binding.modulnameInput.text.toString().trim()
        record.crP = Integer.parseInt(binding.crpInput.text.toString().trim().ifEmpty {
            binding.crpInput.error = getString(R.string.crp_not_empty)
            isValid = false
            "0"
        })
        if (record.crP > 15) {
            binding.crpInput.error = getString(R.string.crp_greater_15)
            isValid = false
        } else if (record.crP < 3) {
            binding.crpInput.error = getString(R.string.crp_less_3)
            isValid = false
        }

        record.note = Integer.parseInt(binding.noteInput.text.toString().trim().ifEmpty {
            binding.noteInput.error = getString(R.string.crp_less_3)
            isValid = false
            "0"
        })

        if (record.note in 1..49) {
            binding.noteInput.error = getString(R.string.note_error)
            isValid = false
        }

        record.semester = (if (binding.ssCheckbox.isChecked) "SS_" else "WS_") + binding.year.selectedItem.toString()

        if (isValid) {
            RecordDAO.get(this).persist(record)
            /*if (record.id == -1) {
                RecordDAO.get(this).persist(record)
            } else {
                RecordDAO.get(this).update(record)
            }*/
            finish()
        }
    }
}