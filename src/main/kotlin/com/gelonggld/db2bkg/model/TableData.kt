package com.gelonggld.db2bkg.model

import androidx.compose.runtime.mutableStateOf
import com.gelonggld.db2bkg.constants.SqlCons
import com.gelonggld.db2bkg.utils.DBConvertUtil

/**
 * Created by gelon on 2017/9/29.
 */
@Suppress("unused")
class TableData(dbField: DBField?, modelField: ModelField?, private val kt: Boolean) {


    val dName = mutableStateOf(dbField?.name)
    val dType = mutableStateOf(dbField?.type)
    val dNote = mutableStateOf(dbField?.comment)
    var dataNum = 0
    val dExt = mutableStateOf(dbField?.ext)
    val mName = mutableStateOf(modelField?.name)
    val mType = mutableStateOf(modelField?.type)
    val mNote = mutableStateOf(modelField?.comment)
    val mExt = mutableStateOf(modelField?.ext)
    val fieldRang = mutableStateOf(SqlCons.FIELD_LENGTH_DEFAULT)
    val defaultValue = mutableStateOf("null")
    val canNone = mutableStateOf(false)
    val radioState = mutableStateOf(OPT_NONE)

    fun matchType():Boolean {
        if(dType.value == null || mType.value == null) return false
        return DBConvertUtil.match(dType.value!!, mType.value!!,kt)
    }

    companion object {

        var DELETE_FROM_DB = "从数据库删除"
        var ADD_TO_MODEL = "同步到模型"
        var NO_NONE = "不操作"
        var DELETE_FROM_MODEL = "从模型删除"
        var ADD_TO_DB = "同步到数据库"
        var ADD_COMMIT_TO_DB = "同步注释到数据库"
        var ADD_COMMIT_TO_MODEL = "同步注释到模型"

        const val OPT_ADD = 1
        const val OPT_DELETE = 2
        const val OPT_NONE = 3

    }

}
