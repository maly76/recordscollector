package com.example.leistungssammler.persistence

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.leistungssammler.model.Record
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class RecordDAO private constructor(private val ctx: Context) {
    private var nextId = 1
    private var records = initRecords()

    fun findAll(): List<Record> = records.toList()

    fun findById(id: Int): Record? = records.find { l -> l.id == id }

    /**
     * Ersetzt das übergebene [Record] Objekt mit einem bereits gespeicherten [Record] Objekt mit gleicher id.
     *
     * @param record
     * @return true = update ok, false = kein [Record] Objekt mit gleicher id im Speicher gefunden
     */
    fun update(record: Record): Boolean {
        val rec = findById(record.id)
        return if (rec != null) {
            records[records.indexOf(rec)] = record
            saveRecords()
            true
        } else {
            false
        }
    }

    /**
     * Persistiert das übergebene [Record] Objekt und liefert die neue id zurück.
     * (Seiteneffekte: record.id = nextId; nextId += 1 )
     *
     * @param record
     * @return neue record id
     */
    fun persist(record: Record): Int {
        record.id = nextId++
        records.add(record)
        saveRecords()
        return record.id
    }

    /**
     * Löscht das übergebene [Record] Objekt anhand der id aus dem Speicher.
     *
     * @param record
     * @return true = ok, false = kein [Record] Objekt mit gleicher id im Speicher gefunden
     */
    fun delete(record: Record): Boolean {
        val rec = findById(record.id)
        return if (rec == null) false else {
            records.remove(rec)
            saveRecords()
            true
        }
    }

    private fun initRecords(): MutableList<Record> {
        val records = mutableListOf<Record>()
        val f = ctx.getFileStreamPath(FILE_NAME)
        if (f.exists()) {
            ctx.openFileInput(FILE_NAME).use { fin ->
                ObjectInputStream(fin).readObject().let { obj ->
                    records.addAll(obj as MutableList<Record>)
                    // init next id
                    if (records.size > 0) {
                        nextId = records.maxOf { it.id ?: 0 } + 1
                    }
                }

            }
        }
        return records;
    }

    private fun saveRecords() {
        ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
            .use { ObjectOutputStream(it).writeObject(records) }
    }

    companion object {
        private const val FILE_NAME = "records.obj"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: RecordDAO? = null

        fun get(context: Context): RecordDAO {
            return INSTANCE ?: synchronized(this) {
                RecordDAO(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
