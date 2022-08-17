package com.gelonggld.db2bkg.model

import androidx.compose.runtime.mutableStateOf
import com.gelonggld.db2bkg.constants.SqlCons
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import com.intellij.psi.PsiField

/**
 * Created by gelon on 2017/10/13.
 */
@Suppress("unused")
class TableCreate(psiField: PsiField,isKt: Boolean) {

    private val mf = FileDispatch.createModelField(psiField)
    var isPri = mutableStateOf(false)
    var fieldName = mutableStateOf(mf.name)
    var fieldType = mutableStateOf(mf.type)
    var fieldComment = mutableStateOf(mf.comment)
    var fieldRang = mutableStateOf(SqlCons.FIELD_LENGTH_DEFAULT)
    var defaultValue = mutableStateOf("NULL")
    var canNull = mutableStateOf(true)
    var autoIncrement = mutableStateOf(false)
    var dbFieldName = mutableStateOf(DBConvertUtil.beanField2DB(mf.name))
     var dbFieldType = mutableStateOf(DBConvertUtil.getBean2DBMapType(mf.type,isKt))
    var isKt = mutableStateOf(isKt)

}
