package com.gelonggld.db2bkg.model

import com.gelonggld.db2bkg.constants.SqlCons
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.gelonggld.db2bkg.utils.StrUtil
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.CheckBox
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup

/**
 * Created by gelon on 2017/9/29.
 */
@Suppress("unused")
class TableData {

    private var mName: SimpleStringProperty?= null
    private var mType: SimpleStringProperty?= null
    private var mNote: SimpleStringProperty?= null
    private var mExt: SimpleStringProperty?= null
    private var dName: SimpleStringProperty?= null
    private var dType: SimpleStringProperty?= null
    private var dNote: SimpleStringProperty?= null
    private var fieldRang: TextField?= null
    private var defaultValue: TextField?= null
    private var canNone: CheckBox?= null


    internal var dataNum: Int = 0
    private var dExt: SimpleStringProperty?= null
    private var deleteOpt: RadioButton? = null
    private var addOpt: RadioButton? = null
    private var doNone: RadioButton?= null
    private var optGroup: ToggleGroup?= null


    val selectRadio: RadioButton?
        get() {
            if (addOpt == null || deleteOpt == null) {
                return null
            }
            if (addOpt!!.isSelected) {
                return addOpt
            }
            return if (deleteOpt!!.isSelected) {
                deleteOpt
            } else doNone
        }


    constructor(dbField: DBField,kt: Boolean) {
        dName = SimpleStringProperty(dbField.name)
        dType = SimpleStringProperty(dbField.type)
        dNote = SimpleStringProperty(dbField.comment)
        dExt = SimpleStringProperty(if (dbField.ext == null) "" else dbField.ext)
        dataNum = dbField.rang
        initRadio(DELETE_FROM_DB, ADD_TO_MODEL, NO_NONE)

    }


    constructor(modelField: ModelField,kt: Boolean) {
        mName = SimpleStringProperty(modelField.name)
        mType = SimpleStringProperty(modelField.type)
        mExt = SimpleStringProperty(if (modelField.ext == null) "" else modelField.ext)
        mNote = SimpleStringProperty(modelField.comment)
        initRadio(DELETE_FROM_MODEL, ADD_TO_DB, NO_NONE)
        fieldRang = TextField("" + SqlCons.FIELD_LENGTH_DEFAULT)
        defaultValue = TextField("null")
        canNone = CheckBox("可空")
        canNone?.isSelected = true
    }

    constructor(entry: kotlin.collections.Map.Entry<ModelField, DBField>,kt: Boolean) {
        dName = SimpleStringProperty(entry.value.name)
        dType = SimpleStringProperty(entry.value.type)
        dNote = SimpleStringProperty(entry.value.comment)
        dataNum = entry.value.rang
        dExt = SimpleStringProperty(if (entry.value.ext == null) "" else entry.value.ext)
        mName = SimpleStringProperty(entry.key.name)
        mType = SimpleStringProperty(entry.key.type)
        mNote = SimpleStringProperty(entry.key.comment)
        mExt = SimpleStringProperty(if (entry.key.ext == null) "" else entry.key.ext)
        if (DBConvertUtil.match(entry.key.type!!, entry.value.type!!,kt)) {
            if (StrUtil.covNull(entry.key.comment).trim() != StrUtil.covNull(entry.value.comment).trim()) {
                initRadio(ADD_COMMIT_TO_MODEL, ADD_COMMIT_TO_DB, NO_NONE)
            }
        } else {
            addOpt = RadioButton("字段类型不匹配，无法操作")
        }
    }

    private fun initRadio(deleteText: String, addText: String, noneText: String) {
        optGroup = ToggleGroup()
        addOpt = RadioButton(addText)
        addOpt!!.toggleGroup = optGroup
        addOpt!!.isSelected = true

        deleteOpt = RadioButton(deleteText)
        deleteOpt!!.toggleGroup = optGroup
        deleteOpt!!.isSelected = true

        doNone = RadioButton(noneText)
        doNone?.toggleGroup = optGroup
        doNone?.isSelected = true
    }


    fun getDataNum(): Int {
        return dataNum
    }

    fun setDataNum(dataNum: Int): TableData {
        this.dataNum = dataNum
        return this
    }

    fun getmName(): String? {
        return mName?.get()
    }

    fun mNameProperty(): SimpleStringProperty? {
        return mName
    }

    fun setmName(mName: String) {
        this.mName?.set(mName)
    }

    fun getmType(): String? {
        return mType?.get()
    }

    fun mTypeProperty(): SimpleStringProperty? {
        return mType
    }

    fun setmType(mType: String) {
        this.mType?.set(mType)
    }

    fun getFieldRang(): TextField? {
        return fieldRang
    }

    fun setFieldRang(fieldRang: TextField): TableData {
        this.fieldRang = fieldRang
        return this
    }

    fun getDefaultValue(): TextField? {
        return defaultValue
    }

    fun setDefaultValue(defaultValue: TextField): TableData {
        this.defaultValue = defaultValue
        return this
    }

    fun getCanNone(): CheckBox? {
        return canNone
    }

    fun setCanNone(canNone: CheckBox): TableData {
        this.canNone = canNone
        return this
    }

    fun getmNote(): String? {
        return mNote?.get()
    }

    fun mNoteProperty(): SimpleStringProperty? {
        return mNote
    }

    fun setmNote(mNote: String) {
        this.mNote?.set(mNote)
    }

    fun getdNote(): String? {
        return dNote?.get()
    }

    fun dNoteProperty(): SimpleStringProperty? {
        return dNote
    }

    fun setdNote(dNote: String) {
        this.dNote?.set(dNote)
    }

    fun getmExt(): String? {
        return mExt?.get()
    }

    fun mExtProperty(): SimpleStringProperty? {
        return mExt
    }

    fun setmExt(mExt: String) {
        this.mExt?.set(mExt)
    }

    fun getdName(): String? {
        return dName?.get()
    }

    fun dNameProperty(): SimpleStringProperty? {
        return dName
    }

    fun setdName(dName: String) {
        this.dName?.set(dName)
    }

    fun getdType(): String? {
        return dType?.get()
    }

    fun dTypeProperty(): SimpleStringProperty? {
        return dType
    }

    fun setdType(dType: String) {
        this.dType?.set(dType)
    }

    fun getdExt(): String? {
        return dExt?.get()
    }

    fun dExtProperty(): SimpleStringProperty? {
        return dExt
    }

    fun setdExt(dExt: String) {
        this.dExt?.set(dExt)
    }

    fun getDeleteOpt(): RadioButton? {
        return deleteOpt
    }

    fun setDeleteOpt(deleteOpt: RadioButton): TableData {
        this.deleteOpt = deleteOpt
        return this
    }

    fun getAddOpt(): RadioButton? {
        return addOpt
    }

    fun setAddOpt(addOpt: RadioButton): TableData {
        this.addOpt = addOpt
        return this
    }

    fun getDoNone(): RadioButton? {
        return doNone
    }

    fun setDoNone(doNone: RadioButton): TableData {
        this.doNone = doNone
        return this
    }

    fun getOptGroup(): ToggleGroup? {
        return optGroup
    }

    fun setOptGroup(optGroup: ToggleGroup): TableData {
        this.optGroup = optGroup
        return this
    }

    companion object {

        var DELETE_FROM_DB = "从数据库删除"
        var ADD_TO_MODEL = "同步到模型"
        var NO_NONE = "不操作"
        var DELETE_FROM_MODEL = "从模型删除"
        var ADD_TO_DB = "同步到数据库"
        var ADD_COMMIT_TO_DB = "同步注释到数据库"
        var ADD_COMMIT_TO_MODEL = "同步注释到模型"
    }

}
