package com.example.leistungssammler.persistence

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.example.leistungssammler.R
import com.example.leistungssammler.databinding.ActivityRecordsBinding
import com.example.leistungssammler.model.Record
import com.example.leistungssammler.model.Statistik

class RecordsActivity : AppCompatActivity() {
    private lateinit var stat: Statistik
    private lateinit var binding: ActivityRecordsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recordListView.emptyView = binding.recordListEmptyView
    }

    override fun onStart() {

        super.onStart()

        val records = RecordDAO.get(this).findAll()
        //val records = listOf(Record(1))

        refreshStat(records)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, records)
        binding.recordListView.adapter = adapter
    }

    fun refreshStat(records: List<Record>){
        val sum = records.size
        val weighting50 = records.filter { l -> l.weighting50 }.size
        val sumCrP = records.sumOf { it.crP }
        val ivgNote = records.sumOf { it.note / sum }
        stat = Statistik(sum, weighting50, sumCrP, ivgNote)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.records, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.add_record_btn, R.id.add_record_btn2 -> {
                val intent = Intent(this, RecordFormActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.stat_show_btn -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.statistic)
                    .setMessage(stat.toString())
                    .setNeutralButton(R.string.close, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}