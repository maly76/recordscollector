package com.example.leistungssammler.viewmodels

import android.app.Application
import android.os.Build
import android.provider.VoicemailContract
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.leistungssammler.model.Record
import com.example.leistungssammler.model.Stats
import com.example.leistungssammler.persistence.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.net.ssl.SSLEngineResult

class RecordsViewModel(app: Application): AndroidViewModel(app) {

    private val recordDao = AppDatabase.getDb(app).recordDao()

    val records: LiveData<List<Record>> = recordDao.findAllSync()

    @RequiresApi(Build.VERSION_CODES.O)
    val statistic: LiveData<Result> = records.switchMap { records ->
        liveData {
            emit(Result.inProgress())
            withContext(Dispatchers.Default) {
                emit(Result.success(Stats(records)))
            }
        }
    }
}

class Result(val status: Status, val stats: Stats? = null) {
    enum class Status {
        SUCCESS, IN_PROGRESS
    }

    companion object{
        fun success(data: Stats?) = Result(Status.SUCCESS, data)
        fun inProgress() = Result(Status.IN_PROGRESS)
    }
}