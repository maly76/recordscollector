package com.example.leistungssammler.model

import android.app.AlertDialog
import kotlin.math.round

class Statistik (_recordsSum: Int, _records_50_Sum: Int, _crPSum: Int, _ivgNote: Int) {
    val recordsSum =_recordsSum
    val records_50_Sum = _records_50_Sum
    val crPSum = _crPSum
    val ivgNote = _ivgNote
    val sumCrPDes = 180 - _crPSum

    override fun toString(): String {
        return "Leistungen $recordsSum \n" +
                "50% Leistungen $records_50_Sum \n" +
                "Summe CrP $crPSum \n" +
                "CrP bis Ziel $sumCrPDes \n" +
                "Durchschnitt $ivgNote"
    }
}