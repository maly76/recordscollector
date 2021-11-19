package com.example.leistungssammler.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.leistungssammler.model.Module

@Dao
interface ModuleDAO {
    @Query("SELECT * FROM module")
    fun findAll(): List<Module>

    @Query("SELECT * FROM module")
    fun findAllSync(): LiveData<List<Module>>

    @Query("SELECT * FROM module WHERE id = :id")
    fun findById(id: Int): Module?

    @Query("SELECT * FROM module WHERE id = :id")
    fun findByIdSync(id: Int): LiveData<Module>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(module: Module): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun persist(module: Module): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun persistAll(modules: List<Module>)

    @Delete
    fun delete(module: Module): Int

    @Query("DELETE FROM module")
    fun deleteAll()
}