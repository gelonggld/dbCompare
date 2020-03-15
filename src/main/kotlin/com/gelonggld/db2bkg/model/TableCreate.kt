package com.gelonggld.db2bkg.model

import com.gelonggld.db2bkg.constants.SqlCons
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import com.intellij.psi.PsiField
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField

/**
 * Created by gelon on 2017/10/13.
 */
@Suppress("unused")
class TableCreate {

    private var isPri: CheckBox? = null
    private var fieldName: SimpleStringProperty? = null
    private var fieldType: SimpleStringProperty? = null
    private var fieldComment: SimpleStringProperty? = null
    private var fieldRang: TextField? = null
    private var defaultValue: TextField? = null
    private var canNull: CheckBox? = null
    private var autoIncrement: CheckBox? = null
    private var dbFieldName: SimpleStringProperty? = null
    private var dbFieldType: SimpleStringProperty? = null
    var isKt = false

    constructor(mf: ModelField,isKt: Boolean) {
        this.isKt = isKt
        this.fieldName = SimpleStringProperty(mf.name)
        this.fieldType = SimpleStringProperty(mf.type)
        this.fieldComment = SimpleStringProperty(mf.comment)

        fieldRang = TextField("" + SqlCons.FIELD_LENGTH_DEFAULT)
        defaultValue = TextField("NULL")
        canNull = CheckBox("可空")
        canNull?.isSelected = true
        dbFieldName = SimpleStringProperty(DBConvertUtil.beanField2DB(mf.name!!))
        dbFieldType = SimpleStringProperty(DBConvertUtil.getBean2DBMapType(mf.type,isKt))
        isPri = CheckBox("主键")
        autoIncrement = CheckBox("自增长")
        isPri!!.selectedProperty().addListener { _, _, newValue ->
            if (newValue!!) {
                canNull?.isSelected = false
            }
        }
    }

    fun isPri(): Boolean {
        return isPri != null && isPri!!.isSelected
    }


    constructor(psiField: PsiField,isKt: Boolean) : this(FileDispatch.createModelField(psiField),isKt)

    fun getIsPri(): CheckBox? {
        return isPri
    }

    fun setIsPri(isPri: CheckBox): TableCreate {
        this.isPri = isPri
        return this
    }

    fun getFieldName(): String? {
        return fieldName?.get()
    }

    fun fieldNameProperty(): SimpleStringProperty? {
        return fieldName
    }

    fun setFieldName(fieldName: String) {
        this.fieldName?.set(fieldName)
    }

    fun getFieldType(): String? {
        return fieldType?.get()
    }

    fun fieldTypeProperty(): SimpleStringProperty? {
        return fieldType
    }

    fun setFieldType(fieldType: String) {
        this.fieldType?.set(fieldType)
    }

    fun getFieldRang(): TextField? {
        return fieldRang
    }

    fun setFieldRang(fieldRang: TextField): TableCreate {
        this.fieldRang = fieldRang
        return this
    }

    fun getDefaultValue(): TextField? {
        return defaultValue
    }

    fun setDefaultValue(defaultValue: TextField): TableCreate {
        this.defaultValue = defaultValue
        return this
    }

    fun getCanNull(): CheckBox? {
        return canNull
    }

    fun setCanNull(canNull: CheckBox): TableCreate {
        this.canNull = canNull
        return this
    }

    fun getAutoIncrement(): CheckBox? {
        return autoIncrement
    }

    fun getFieldComment(): String? {
        return fieldComment?.get()
    }

    fun fieldCommentProperty(): SimpleStringProperty? {
        return fieldComment
    }

    fun setFieldComment(fieldComment: String) {
        this.fieldComment?.set(fieldComment)
    }

    fun setAutoIncrement(autoIncrement: CheckBox): TableCreate {
        this.autoIncrement = autoIncrement
        return this
    }

    fun getDbFieldName(): String? {
        return dbFieldName?.get()
    }

    fun dbFieldNameProperty(): SimpleStringProperty? {
        return dbFieldName
    }

    fun setDbFieldName(dbFieldName: String) {
        this.dbFieldName?.set(dbFieldName)
    }

    fun getDbFieldType(): String? {
        return dbFieldType?.get()
    }

    fun dbFieldTypeProperty(): SimpleStringProperty? {
        return dbFieldType
    }

    fun setDbFieldType(dbFieldType: String) {
        this.dbFieldType?.set(dbFieldType)
    }
}
