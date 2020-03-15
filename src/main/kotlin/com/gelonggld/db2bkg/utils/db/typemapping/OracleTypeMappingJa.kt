package com.gelonggld.db2bkg.utils.db.typemapping


import com.gelonggld.db2bkg.exceptions.TypeMapException
import java.util.HashMap

/**
 * Created by gelon on 2017/10/27.
 */
object OracleTypeMappingJa {

    private var db2BeanMapJa: MutableMap<String, String>? = null
    private var bean2DBMapJa: MutableMap<String, String>? = null

    fun init() {
        db2BeanMapJa = HashMap()
        bean2DBMapJa = HashMap()
        putBeanMapDBJa("String", "VARCHAR2", "CHAR", "LONG", "NVARCHAR2")
        putDBMapBeanJa("NUMBER", "Integer", "java.math.BigDecimal", "Boolean", "Byte", "Short", "Long", "Float", "Double")
        putBeanMapDBJa("java.util.Date", "DATE", "TIMESTAMP")


    }


    fun getDb2BeanMapGet(dbType: String): String {
        if (db2BeanMapJa == null) {
            init()
        }
        return db2BeanMapJa!![dbType] ?: throw TypeMapException("错误的db类型--$dbType")
    }

    fun getBean2DBMapGet(beanType: String): String {
        if (bean2DBMapJa == null) {
            init()
        }
        return bean2DBMapJa!![beanType] ?: throw TypeMapException("错误的bean类型--$beanType")
    }

    fun putBeanMapDBJa(onlyValue: String, vararg values: String) {
        bean2DBMapJa!![onlyValue] = values[0]
        for (value in values) {
            db2BeanMapJa!![value] = onlyValue
        }
    }


    fun putDBMapBeanJa(onlyValue: String, vararg values: String) {
        db2BeanMapJa!![onlyValue] = values[0]
        for (value in values) {
            bean2DBMapJa!![value] = onlyValue
        }
    }


}
