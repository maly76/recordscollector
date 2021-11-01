package com.example.leistungssammler.model

class Record (_modulNum: String, _modulName: String, _year: Int,
              _isSummerTerm: Boolean, _isHalfWeighted: Boolean, _crp: Int, _mark: Int
             ): java.io.Serializable
{
    var id = -1;
    var moduleNum: String = _modulNum
    var moduleName: String = _modulName
    var year = _year
    var isSummerTerm = _isSummerTerm
    var crp: Int = _crp
    var mark: Int = _mark
    var isHalfWeighted = _isHalfWeighted

    override fun toString(): String {
        return "$moduleName $moduleNum ($mark $crp crp)"
    }

    constructor() : this("null", "null", 0, true, true, 0, 0) {
    }
}