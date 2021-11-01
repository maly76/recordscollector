package com.example.leistungssammler

import com.example.leistungssammler.model.Record
import com.example.leistungssammler.model.Stats
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class StatsTest {
    @Test
    fun testSumCrp() {
        Assert.assertEquals(24, STATS.sumCrp)
    }

    @Test
    fun testCrpToEnd() {
        Assert.assertEquals(156, STATS.crpToEnd)
    }

    @Test
    fun testSumHalfWeighted() {
        Assert.assertEquals(2, STATS.sumHalfWeighted)
    }

    @Test
    fun testAverageMark() {
        Assert.assertEquals(83, STATS.averageMark.toLong())
    }

    companion object {
        private lateinit var STATS: Stats

        @BeforeClass
        @JvmStatic
        fun setUp() {
            STATS = Stats(
                listOf(
                    Record("CS1013", "Objektorientierte Programmierung", 2016, true, true, 6, 73),
                    Record("MN1007", "Diskrete Mathematik", 2016, false, true, 6, 81),
                    Record("CS1019", "Compilerbau", 2017, false, false, 6, 81),
                    Record("CS1020", "Datenbanksysteme", 2017, false, false, 6, 92)
                )
            )
        }
    }
}