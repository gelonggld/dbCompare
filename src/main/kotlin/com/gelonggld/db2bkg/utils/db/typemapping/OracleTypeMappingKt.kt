package com.gelonggld.db2bkg.utils.db.typemapping


import com.gelonggld.db2bkg.convertInt
import com.gelonggld.db2bkg.exceptions.TypeMapException
import java.util.HashMap

/**
 * Created by gelon on 2017/10/27.
 */
object OracleTypeMappingKt {


    private var db2BeanMapKt: MutableMap<String, String>? = null
    private var bean2DBMapKt: MutableMap<String, String>? = null


    fun init() {


        db2BeanMapKt = HashMap()
        bean2DBMapKt = HashMap()
        putBeanMapDBKt("String", "VARCHAR2", "CHAR", "LONG", "NVARCHAR2")
        putDBMapBeanKt("NUMBER", "Int", "java.math.BigDecimal", "Boolean", "Byte", "Short", "Long", "Float", "Double")
        putBeanMapDBKt("java.util.Date", "DATE", "TIMESTAMP")

    }


    fun getDb2BeanMapGet(dbType: String): String {
        if (db2BeanMapKt == null) {
            init()
        }
        return db2BeanMapKt!![dbType.convertInt()] ?: throw TypeMapException("错误的db类型--$dbType")
    }

    fun getBean2DBMapGet(beanType: String): String {
        if (db2BeanMapKt == null) {
            init()
        }
        return bean2DBMapKt!![beanType] ?: throw TypeMapException("错误的bean类型--$beanType")
    }


    private fun putBeanMapDBKt(onlyValue: String, vararg values: String) {
        bean2DBMapKt!![onlyValue] = values[0]
        for (value in values) {
            db2BeanMapKt!![value] = onlyValue
        }
    }


    private fun putDBMapBeanKt(onlyValue: String, vararg values: String) {
        db2BeanMapKt!![onlyValue] = values[0]
        for (value in values) {
            bean2DBMapKt!![value] = onlyValue
        }
    }


}
