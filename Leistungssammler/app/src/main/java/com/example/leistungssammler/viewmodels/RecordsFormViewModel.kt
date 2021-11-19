package com.example.leistungssammler.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.leistungssammler.R
import com.example.leistungssammler.model.Module
import com.example.leistungssammler.model.Record
import com.example.leistungssammler.persistence.AppDatabase

class RecordsFormViewModel(app: Application): AndroidViewModel(app) {

    val moduleDAO = AppDatabase.getDb(app).moduleDao()
    val modules: LiveData<List<Module>> = moduleDAO.findAllSync()
    var record: Record? = null
    var names = arrayOf<String>()
    var years = arrayOf<String>()

    //val statistic: LiveData<Stats> = Transformations.map(records) { Stats(it) }
}