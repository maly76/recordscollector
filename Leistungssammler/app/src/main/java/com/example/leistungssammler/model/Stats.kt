package com.example.leistungssammler.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.sql.Time
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import java.time.temporal.ChronoUnit.SECONDS;

@RequiresApi(Build.VERSION_CODES.O)
class Stats(records: List<Record>) {
    var recordsSum = 0
    var sumHalfWeighted = 0
    var sumCrp = 0
    var averageMark = 0
    var crpToEnd = 180

    init {
        val before = LocalTime.now()

        recordsSum = records.size
        sumHalfWeighted = records.filter { l -> l.isHalfWeighted }.size
        sumCrp = records.sumOf { it.crp }

        averageMark = records.sumOf { it.mark * if(it.isHalfWeighted) it.crp / 2 else it.crp } /
                      records.sumOf { if (it.isHalfWeighted) it.crp / 2 else it.crp }
        crpToEnd -= sumCrp

        val timeDiff = getTimeDiff(LocalTime.now(), before)
        if (timeDiff < 3) {
            TimeUnit.SECONDS.sleep((3 - timeDiff).toLong())
        }
    }

    private fun getTimeDiff(time1: LocalTime, time2: LocalTime): Int {
        return SECONDS.between(time2, time1).toInt()
    }

    override fun toString(): String {
        return "Leistungen $recordsSum \n" +
                "50% Leistungen $sumHalfWeighted \n" +
                "Summe CrP $sumCrp \n" +
                "CrP bis Ziel $crpToEnd \n" +
                "Durchschnitt $averageMark"
    }
}