package com.example.leistungssammler.model

class Stats(records: List<Record>) {
    var recordsSum = 0
    var sumHalfWeighted = 0
    var sumCrp = 0
    var averageMark = 0
    var crpToEnd = 180

    init {
        recordsSum = records.size
        sumHalfWeighted = records.filter { l -> l.isHalfWeighted }.size
        sumCrp = records.sumOf { it.crp }

        averageMark = records.sumOf { it.mark * if(it.isHalfWeighted) it.crp / 2 else it.crp } /
                      records.sumOf { if (it.isHalfWeighted) it.crp / 2 else it.crp }
        crpToEnd -= sumCrp
    }

    override fun toString(): String {
        return "Leistungen $recordsSum \n" +
                "50% Leistungen $sumHalfWeighted \n" +
                "Summe CrP $sumCrp \n" +
                "CrP bis Ziel $crpToEnd \n" +
                "Durchschnitt $averageMark"
    }
}