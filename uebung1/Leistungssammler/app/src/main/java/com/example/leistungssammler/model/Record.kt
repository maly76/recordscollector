package com.example.leistungssammler.model

data class Record (val _id: Int): java.io.Serializable {
    var id = _id;
    var modulnummer: String = "null"
    var modulname: String = "null"
    var semester: String = "WS_"
    var crP: Int = 0
    var note: Int = 0
    var weighting50 = false

    override fun toString(): String {
        return "$modulname $modulnummer ($semester $note $crP crp)"
    }
}