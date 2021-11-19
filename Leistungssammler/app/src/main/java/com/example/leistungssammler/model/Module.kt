package com.example.leistungssammler.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
@kotlinx.serialization.Serializable
class Module {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var nr: String? = null
    var name: String? = null
    var crp: Int? = null

    override fun toString(): String {
        return name!!
    }
}