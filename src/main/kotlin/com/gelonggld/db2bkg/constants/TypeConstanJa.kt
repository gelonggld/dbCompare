package com.gelonggld.db2bkg.constants

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

/**
 * Created by gelon on 2017/10/27.
 */
object TypeConstanJa {

    private var typeMap: BiMap<String, String>? = null

    fun init() {
        typeMap = HashBiMap.create()
        typeMap!!["Byte"] = "byte"
        typeMap!!["Integer"] = "int"
        typeMap!!["Short"] = "short"
        typeMap!!["Long"] = "long"
        typeMap!!["Boolean"] = "boolean"
        typeMap!!["Character"] = "char"
        typeMap!!["Float"] = "float"
        typeMap!!["Double"] = "double"
    }

    fun getTypeMap(): BiMap<String, String>? {
        if (typeMap == null) {
            init()
        }
        return typeMap
    }

}
