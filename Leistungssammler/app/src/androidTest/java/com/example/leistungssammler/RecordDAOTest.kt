package com.example.leistungssammler

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.leistungssammler.model.Record
import com.example.leistungssammler.persistence.RecordDAO
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordDAOTest {

    @Before
    fun persistTestData() {
        TEST_RECORDS.forEach { RECORD_DAO.persist(it) }
    }

    @After
    fun deleteTestData() {
        TEST_RECORDS.forEach { RECORD_DAO.delete(it) }
    }

    @Test
    fun findAll() {
        val records = RECORD_DAO.findAll()
        Assert.assertTrue(records.size >= TEST_RECORDS.size)
    }

    @Test
    fun findById() {
        val records = RECORD_DAO.findAll().toMutableList()
        Assert.assertTrue(records.size >= TEST_RECORDS.size)
        records.shuffle()
        val record = records.first().id?.let { RECORD_DAO.findById(it) }
        Assert.assertNotNull(record)
        Assert.assertEquals(records.first().moduleNum, record?.moduleNum)
    }

    @Test
    fun update() {
        val moduleNum = "MN1007 TEST"
        val records = RECORD_DAO.findAll()
        val record = records.firstOrNull { moduleNum == it.moduleNum }
        Assert.assertNotNull(record)

        record?.let {
            it.moduleName = "modified! $it.moduleName"
            it.year = 2015
            it.isSummerTerm = true
            it.isHalfWeighted = false
            it.crp = 3
            it.mark = 51
            Assert.assertTrue(RECORD_DAO.update(it))

            val recordUpdate = RECORD_DAO.findById(record.id!!)
            Assert.assertNotNull(recordUpdate)

            Assert.assertEquals(it.moduleNum, recordUpdate?.moduleNum)
            Assert.assertEquals(it.moduleName, recordUpdate?.moduleName)
            Assert.assertEquals(it.year, recordUpdate?.year)
            Assert.assertEquals(it.isSummerTerm, recordUpdate?.isSummerTerm)
            Assert.assertEquals(it.isHalfWeighted, recordUpdate?.isHalfWeighted)
            Assert.assertEquals(it.crp, recordUpdate?.crp)
            Assert.assertEquals(it.mark, recordUpdate?.mark)

            val recordNoId =
                Record("CS1016 TEST", "Programmierung interaktiver Systeme TEST", 2016, true, true, 6, 95)
            Assert.assertFalse(RECORD_DAO.update(recordNoId))
        }
    }

    @Test
    fun persist() {
        val record = Record("CS1022 TEST", "Betriebssysteme TEST", 2017, false, false, 6, 90)
        val newId = RECORD_DAO.persist(record)

        val recordNew = RECORD_DAO.findById(newId)
        Assert.assertNotNull(recordNew)

        recordNew?.let {
            Assert.assertEquals(record.id, it.id)
            Assert.assertEquals(record.moduleNum, it.moduleNum)
            Assert.assertEquals(record.moduleName, it.moduleName)
            Assert.assertEquals(record.year, it.year)
            Assert.assertEquals(record.isSummerTerm, it.isSummerTerm)
            Assert.assertEquals(record.isHalfWeighted, it.isHalfWeighted)
            Assert.assertEquals(record.crp, it.crp)
            Assert.assertEquals(record.mark, it.mark)
            RECORD_DAO.delete(record)
        }

    }

    companion object {
        private val TEST_RECORDS = mutableListOf<Record>()
        private val RECORD_DAO =
            RecordDAO.get(InstrumentationRegistry.getInstrumentation().targetContext)

        @BeforeClass
        @JvmStatic
        fun initTestData() {
            TEST_RECORDS.add(Record("CS1013 TEST", "Objektorientierte Programmierung TEST", 2016, true, true, 6, 73))
            TEST_RECORDS.add(Record("MN1007 TEST", "Diskrete Mathematik TEST", 2016, false, true, 6, 81))
            TEST_RECORDS.add(Record("CS1019 TEST", "Compilerbau TEST", 2017, false, false, 6, 81))
            TEST_RECORDS.add(Record("CS1020 TEST", "Datenbanksysteme TEST", 2017, false, false, 6, 92))
        }
    }
}
